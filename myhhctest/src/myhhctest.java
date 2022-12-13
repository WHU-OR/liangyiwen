import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

public class myhhctest {//类功能：建立模型并求解
    Data data;          //定义类Data的对象
    IloCplex model;        //定义cplex内部类的对象

    //声明决策变量
    public IloNumVar[][][] x;  //x[i][j][k]表示弧arcs[i][j]被车辆k访问
    public IloNumVar[] y;     //停靠点被使用时 为1
    public IloNumVar[][] z;    //客户被分配到停靠点时 为1
    public IloNumVar[][] u;    //车辆在停靠点开始服务的时间
    public IloNumVar[] d;      //停靠点 完成服务所需要的时间
    public IloNumVar[] q;      //停靠点 对应的负载值 有正有负
    public IloNumVar[][] w;    //车辆离开停靠点的时候车上的人数
    double total_cost;        //目标值object  min cost
    Solution solution;       //记录解情况
    static Get_example  example= new Get_example();

    public myhhctest ( Data data ){ this.data = data; }

    public void solve_model() throws IloException{
        //函数功能：解模型，并生成车辆路径和得到目标值

        //初始化变量  用于生成solution
        ArrayList<ArrayList<Integer>> routes = new ArrayList<>();    //定义车辆路径
        double[] stop_times = new double [data.stop_num];//每个D停靠点完成服务需要的时间
        int[] stop_demands = new int [2+2*data.stop_num];//每个点对应的负载值
        ArrayList<ArrayList<Double>> start_serve = new ArrayList<>();//车的开始服务时间序列
        for (int k = 0; k < data.vec_num; k++) {
            //初始化二级数组，数组长度为车辆数k
            ArrayList<Integer> t1 = new ArrayList<>();  //定义一个对象为int型的链表
            ArrayList<Double> t2 = new ArrayList<>();  //定义一个对象为double型的链表
            routes.add(t1);                //将上述定义的链表加入到链表routes中
            start_serve.add(t2);              //同上
        }

        //判断cplex建立的模型是否可解
        if(!model.solve()){//模型不可解
            System.out.println("can not be solved !!!");
            return;
        }else{ //模型可解，生成车辆路径
            //打印决策变量值
            System.out.println();
            for (int i=0;i<data.stop_num;++i){
                System.out.print("y["+i+"]:");
                System.out.print(model.getValue(y[i])+"  ");
            }
            System.out.println();
            for (int i=0;i<data.stop_num;++i) {
                stop_times[i] =model.getValue(d[i]);
                System.out.print("d["+i+"]:");
                System.out.print(model.getValue(d[i])+"    ");
            }
            System.out.println();
            for (int i=0;i<2+2*data.stop_num;++i) {
                stop_demands[i] =(int)model.getValue(q[i]);
                System.out.print("q["+i+"]:");
                System.out.print(model.getValue(q[i])+"    ");
            }
            System.out.println();
            for (int i=0;i<data.stop_num;++i){
                for(int c=0;c<data.cus_num;++c) {
                    System.out.print("z["+i+"]["+c+"]:");
                    System.out.print(model.getValue(z[i][c]) + "    ");
                }
            }
            System.out.println();
            for (int i=0;i<2+2*data.stop_num;++i){
                for (int j=0;j<2+2*data.stop_num;++j)
                    if(data.arcs[i][j]==1)
                    for(int k=0;k<data.vec_num;++k) {
                    System.out.print("x["+i+"]["+j+"]["+k+"]:");
                    System.out.print(model.getValue(x[i][j][k]) +"   ");
                }
            }
            System.out.println();
            for (int i=0;i<2+2*data.stop_num;++i){
                for(int k=0;k<data.vec_num;++k) {
                    System.out.print("u["+i+"]["+k+"]:");
                    System.out.print(data.double_truncate(model.getValue(u[i][k])) +"   ");
                }
            }
            System.out.println();
            for (int i=0;i<2+2*data.stop_num;++i){
                for(int k=0;k<data.vec_num;++k) {
                    System.out.print("w["+i+"]["+k+"]:");
                    System.out.print(data.double_truncate(model.getValue(w[i][k])) +"   ");
                }
            }
            System.out.println();
            System.out.println();
            //生成路径
            for(int k=0; k<data.vec_num; k++){
                boolean terminate = true;
                int i=0;
                routes.get(k).add(0);//对每一个k，从点0开始
                start_serve.get(k).add(.0);
                while(terminate){
                    for(int j=0;j<2+2*data.stop_num;j++){
                        if (data.arcs[i][j]==1 && model.getValue(x[i][j][k])==1){
                            routes.get(k).add(j);//把下一个节点加入路径中
                            start_serve.get(k).add(model.getValue(u[j][k]));
                            i=j; //更新检索起点
                            break;
                        }
                        if(i==0 && j==1+2*data.stop_num)
                            terminate = false;
                    }
                    if ( i== 1+2*data.stop_num)//检索完毕 就终止
                        terminate = false;
                }
            }

        }
        solution = new Solution(data,routes,start_serve,stop_times,stop_demands);
        total_cost = model.getObjValue();
        System.out.println("routes="+solution.routes);
        //System.out.println("serve times ="+solution.start_serve);
    }
    private void build_model() throws IloException {
        //函数功能：根据VRPTW数学模型建立VRPTW的cplex模型
        this.model = new IloCplex();
        model.setOut(null);
        //variables 决策变量
        x= new IloNumVar[2+2*data.stop_num][2+2*data.stop_num][data.vec_num];
        y= new IloNumVar[data.stop_num];//0-1
        z= new IloNumVar[data.stop_num][data.cus_num];//0-1
        u= new IloNumVar[2+2*data.stop_num][data.vec_num];//时间
        d= new IloNumVar[data.stop_num];//时间
        q= new IloNumVar[2+2*data.stop_num];//负载
        w= new IloNumVar[2+2*data.stop_num][data.vec_num];//负载
        //定义cplex变量x和w的数据类型及取值范围
        for(int i=0;i<data.stop_num;i++){//定义在集合D上
            y[i] = model.numVar(0,1,IloNumVarType.Int,"y"+i);
            d[i] = model.numVar(0,1e15,IloNumVarType.Float,"d"+i);
            for(int c=0;c<data.cus_num;c++)//集合C上
                z[i][c] = model.numVar(0,1,IloNumVarType.Int,"z"+i+","+c);
        }
        for(int i=0;i<2+2*data.stop_num;i++){
            q[i] = model.numVar((-data.Q),data.Q,IloNumVarType.Int,"q"+i);
            for(int k=0;k<data.vec_num;k++){
                u[i][k]=model.numVar(0,1e15,IloNumVarType.Float,"u"+i+","+k);
                w[i][k]=model.numVar(0,data.Q,IloNumVarType.Int,"w"+i+","+k);
            }
            for(int j=0;j<2+2*data.stop_num;j++){
                if(data.arcs[i][j]==0)
                    x[i][j]=null;
                else{
                    for(int k=0;k<data.vec_num;k++)
                        x[i][j][k]=model.numVar(0,1,IloNumVarType.Int,"x"+i+","+j+","+k);
                }
            }
        }
        //加入目标函数
        IloNumExpr obj = model.numExpr();
        IloNumExpr t1 = model.numExpr();
        IloNumExpr t2 = model.numExpr();
        IloNumExpr t3 = model.numExpr();
        for(int i = 0; i < 2+2*data.stop_num; i++){
            for(int j = 0; j < 2+2*data.stop_num; j++){
                if (data.arcs[i][j]==0)   continue;
                for(int k = 0; k < data.vec_num; k++)
                    //model.prod 参数是决策变量数组和决策变量的系数数组
                    t1 = model.sum(t1, model.prod(data.car_time[i][j], x[i][j][k]));
            }
        }
        for(int k = 0; k < data.vec_num; k++){
            t2 =  model.sum(t2, model.sum(1, model.prod(-1, x[0][1+2*data.stop_num][k])));
        }
        for(int k = 0; k < data.vec_num; k++){
            t3 =  model.sum(t3, u[1+2*data.stop_num][k]);
        }
        obj = model.sum(t1, model.prod(data.F, t2));
        //obj = model.sum(obj, t3);
        model.addMinimize(obj);

        //加入约束
        //(2)每个客户都被分配给一个停靠点
        for(int c=0;c<data.cus_num;++c){
            IloNumExpr expr1 = model.numExpr();//cplex表达式
            for(int i=0;i<data.stop_num;i++){
                expr1 = model.sum(expr1, z[i][c]);
            }
            model.addEq(expr1, 1);
        }
        //(3)只有停靠点启用时才能被分配客户
        for(int c=0;c<data.cus_num;++c){
            for(int i=0;i<data.stop_num;i++){
                 model.addLe(z[i][c],y[i]);
            }
        }
        //(4)每个下车停靠点完成对应服务所需的时间
        for(int i=0;i<data.stop_num;i++){//对于每一个停靠点
            IloNumExpr temp2;
            for(int c=0;c<data.cus_num;c++) {//找出时间最长的一个客户
                double temp3= 2*data.human_time[i][c] + data.cus_times[c];
                temp2 = model.prod(temp3, z[i][c]);
                model.addLe(temp2,d[i]);
            }
        }
        //(5)(6)每个停靠点的负载值
        for(int i=0;i<data.stop_num;i++){
            IloNumExpr expr2 = model.numExpr();
            for(int c=0;c<data.cus_num;c++){
                expr2 =model.sum(expr2, z[i][c]);
            }
            model.addEq(q[i+1], model.prod(-1,expr2));
            model.addEq(q[i+1+data.stop_num], expr2);
        }
        model.addEq(q[0],0); //中心负载值为0
        model.addEq(q[1+2*data.stop_num],0);
        double M=200;
        //(7）客户分配到距离自身最近的停靠点
        for(int c=0;c<data.cus_num;c++){
            double t_min = data.human_time[0][c];
            for(int i=0;i<data.stop_num;++i){
                if(t_min > data.human_time[i][c])
                    t_min = data.human_time[i][c];
            }
            for(int i=0;i<data.stop_num;++i){
                IloNumExpr expr2 = model.numExpr();
                expr2=model.prod(M, model.diff(1,z[i][c]));
                model.addLe((data.human_time[i][c]-t_min),expr2 );
            }
        }
        //(7)被启用的停靠点仅一车访问
        for(int i=1;i<=data.stop_num;i++){
            IloNumExpr expr3 = model.numExpr();
            IloNumExpr expr4 = model.numExpr();
            for(int j = 0; j < 2+2*data.stop_num; j++) {
                if (data.arcs[i][j] == 1) {
                    for (int k = 0; k < data.vec_num; k++)
                        expr3 = model.sum(expr3, x[i][j][k]);
                }
                if(data.arcs[j][i] == 1)
                    for (int k = 0; k < data.vec_num; k++)
                        expr4 = model.sum(expr3, x[j][i][k]);
            }
            model.addEq(expr3,y[i-1]);
            model.addEq(expr4,y[i-1]);
        }
        //(8)一个停靠点的两个副本被同一车访问
        for(int i=1;i<=data.stop_num;i++){
            for(int k=0;k<data.vec_num;k++){
                IloNumExpr expr4 = model.numExpr();
                IloNumExpr expr5 = model.numExpr();
                for(int j = 0; j < 2+2*data.stop_num; j++){
                    if(data.arcs[i][j]==1 )
                        expr4 = model.sum(expr4, x[i][j][k]);
                    if(data.arcs[i+data.stop_num][j]==1)
                        expr5 = model.sum(expr5, x[i + data.stop_num][j][k]);
                }
                model.addEq(expr4, expr5);
            }
        }
        //(9)从治疗中心出发送人，再接人回到治疗中心，且仅一次
        for(int k=0;k<data.vec_num;k++){
            IloNumExpr expr6 = model.numExpr();
            IloNumExpr expr7 = model.numExpr();
            for(int j = 0; j < 2+2*data.stop_num; j++){
                if(data.arcs[0][j]==1)
                    expr6 = model.sum(expr6, x[0][j][k]);
                if(data.arcs[j][1+2*data.stop_num]==1)
                    expr7 = model.sum(expr7, x[j][1 + 2 * data.stop_num][k]);
            }
            model.addEq(expr6,1);
            model.addEq(expr7,1);
        }
        //(10)除了治疗中心，每个停靠点的流平衡
        for(int k=0;k<data.vec_num;k++){
            for(int i=1;i<=2*data.stop_num;i++){
                IloNumExpr expr8 = model.numExpr();
                IloNumExpr expr9 = model.numExpr();
                for(int j = 0; j < 2+2*data.stop_num; j++){
                    if(data.arcs[i][j]==1)
                        expr8 = model.sum(expr8, x[i][j][k]);
                    if(data.arcs[j][i]==1)
                        expr9 = model.sum(expr9, x[j][i][k]);

                }
                model.addEq(expr8,expr9);
            }
        }
        //(11)时间递推关系
        double Mt=1000;//=========================??????????????????????????
        for(int k=0;k<data.vec_num;k++){
            for(int i=0;i<2+2*data.stop_num;i++){
                for(int j = 0; j < 2+2*data.stop_num; j++){
                    if(data.arcs[i][j]==1) {
                        IloNumExpr expr10 = model.numExpr();
                        IloNumExpr expr11 = model.numExpr();
                        expr10 = model.prod(Mt, model.sum(1, model.prod(-1, x[i][j][k])));
                        expr11 = model.sum(data.car_time[i][j], model.sum(u[i][k], model.prod(-1, u[j][k])));
                        model.addLe(expr11, expr10);
                    }
                }
            }
            model.addEq(u[0][k],0);
        }
        //(13)车容量递推关系
        double M2=20;
        for(int k=0;k<data.vec_num;k++){
            for(int i=0;i<2+2*data.stop_num;i++){
                for(int j = 0; j < 2+2*data.stop_num; j++){
                    if(data.arcs[i][j]==1 ) {
                        IloNumExpr expr10 = model.numExpr();
                        IloNumExpr expr12 = model.numExpr();
                        expr10 = model.prod(M2, model.diff(1, x[i][j][k]));
                        expr12 = model.sum(q[j], model.diff(w[i][k],  w[j][k]));
                        model.addLe(expr12, expr10);
                    }
                }
            }
        }
        //(12)(14)每个节点的车容量限制
        for(int k=0;k<data.vec_num;k++) {
            for (int i = 0; i < 2 + 2 * data.stop_num; i++) {
                model.addLe(0,w[i][k]);
                model.addLe(w[i][k],data.Q);
            }
            model.addEq(w[0][k],w[1+2 * data.stop_num][k]);
        }
        //(15)接人的时候必须已经完成了医疗服务
        for(int k=0;k<data.vec_num;k++){
            for(int i=0;i<data.stop_num;i++){
                IloNumExpr expr13 = model.numExpr();
                expr13 = model.sum(d[i],u[i+1][k]);
                model.addLe(expr13, u[i+1+data.stop_num][k]);
            }
        }
        //(16)接人时医护人员最大等待时间限制
        for(int k=0;k<data.vec_num;k++){
            for(int i=0;i<data.stop_num;i++){
                IloNumExpr expr14 = model.numExpr();
                expr14=model.sum(data.T, model.sum(u[i+1][k], d[i]));
                model.addLe( u[i+1+data.stop_num][k], expr14 );
            }
        }
        //(17)车辆最大行驶时间
        for(int k=0;k<data.vec_num;k++){
            IloNumExpr expr15 = model.numExpr();
            expr15 = model.sum(u[1+2*data.stop_num][k], model.prod(-1,u[0][k]));
            model.addLe(expr15, data.L);
        }

    }
    public static void proData(String path, Data data) throws Exception{
         String line = null;
         String[] substr = null;
         Scanner cin=new Scanner(new BufferedReader(new FileReader(path)));  //读取文件
        for(int i=0;i<4;i++){
            line = cin.nextLine();
        }
        //返回调用字符串对象的一个副本，删除起始和结尾的空格
        substr = line.trim().split(("\\s+")); //以空格为标志将字符串拆分
        //读取数据  初始化参数
        data.cus_num = Integer.parseInt(substr[0]);
        data.stop_num = Integer.parseInt(substr[1]);
        data.vec_num = Integer.parseInt(substr[2]);
        data.T = Integer.parseInt(substr[3]);
        data.Q = Integer.parseInt(substr[4]);
        data.F = Integer.parseInt(substr[5]);
        data.L = Integer.parseInt(substr[6]);
        data.cus_location = new int[data.cus_num][2];  //客户坐标
        data.stop_location = new int[2+2*data.stop_num][2];  //停靠点和中心坐标（包括副本
        data.cus_times = new double[data.cus_num];      //客户需求时间
        data.vehicles = new int[data.vec_num];          //车辆编号
        data.arcs = new int[2+2*data.stop_num][2+2*data.stop_num]; //弧 初始化为0
        data.car_time = new double[2+2*data.stop_num][2+2*data.stop_num];//车行驶时间表
        data.human_time= new double[data.stop_num][data.cus_num];//人在二级路径时间表
        //读入客户信息
        cin.nextLine();
        cin.nextLine();
        for(int i=0; i < data.cus_num ;i++){
            line = cin.nextLine();
            substr = line.trim().split("\\s+");
            data.cus_location[i][0] = Integer.parseInt(substr[1]);
            data.cus_location[i][1] = Integer.parseInt(substr[2]);
            data.cus_times[i] = Integer.parseInt(substr[3]);
        }
        //读入中心和停靠点信息
        cin.nextLine();
        cin.nextLine();
        for(int i=0; i < data.stop_num+1 ;++i){
            line = cin.nextLine();
            substr = line.trim().split("\\s+");
            data.stop_location[i][0] = Integer.parseInt(substr[1]);
            data.stop_location[i][1] = Integer.parseInt(substr[2]);
            if(i !=0 ){//副本坐标
                data.stop_location[i+data.stop_num][0] = data.stop_location[i][0];
                data.stop_location[i+data.stop_num][1] = data.stop_location[i][1];
            }else{
                data.stop_location[1+2*data.stop_num][0] =  data.stop_location[i][0];
                data.stop_location[1+2*data.stop_num][1] =  data.stop_location[i][1];
            }
        }
        cin.close();//关闭流
        //行驶时间矩阵初始化 car_time
        for(int i=0;i<2+2*data.stop_num;i++){
            for(int j=0;j<2+2*data.stop_num;j++){
                if( i==j ){
                    data.car_time[i][j]=0;
                }else if(i!=0 && j!=0 && ( i==j+data.stop_num || j==i+data.stop_num)){//停靠点的两个副本之间
                    data.car_time[i][j]=0;
                }
                else {
                    data.car_time[i][j]=Math.sqrt((data.stop_location[i][0]-data.stop_location[j][0])*(data.stop_location[i][0]-data.stop_location[j][0])+
                            (data.stop_location[i][1]-data.stop_location[j][1])*(data.stop_location[i][1]-data.stop_location[j][1]));
                    data.car_time[i][j]=data.double_truncate(data.car_time[i][j]);//截断精度
                }
            }
        }
        data.car_time[0][1+2*data.stop_num]=0;
        data.car_time[1+2*data.stop_num][0]=0;
        /*距离矩阵满足三角关系
        for (int  k = 0; k <2+2*data.stop_num; k++) {
            for (int i = 0; i <2+2*data.stop_num; i++) {
                for (int j = 0; j < 2+2*data.stop_num; j++) {
                    if (data.car_time[i][j] > data.car_time[i][k] + data.car_time[k][j]) {
                        data.car_time[i][j] = data.car_time[i][k] + data.car_time[k][j];
                    }
                }
            }
        }
        */
        //人步行时间矩阵初始化 human_time
        for (int i=1;i<=data.stop_num;++i){//集合D中
            for(int c=0;c<data.cus_num;++c){
                data.human_time[i-1][c]=Math.sqrt((data.stop_location[i][0]-data.cus_location[c][0])*(data.stop_location[i][0]-data.cus_location[c][0])+
                        (data.stop_location[i][1]-data.cus_location[c][1])*(data.stop_location[i][1]-data.cus_location[c][1]));
                data.human_time[i-1][c]=data.double_truncate(data.human_time[i-1][c]);//截断精度
            }
        }
        //初始化弧集:0->D+n+1; D/p->D/P; P->n+1
        for(int i=1;i<=data.stop_num;i++){
            data.arcs[0][i]=1;
        }
        for(int i=1; i<= 2*data.stop_num;i++){
            for(int j=1; j<= 2*data.stop_num;j++){
                if(i != j)
                    data.arcs[i][j]=1;
                else
                    data.arcs[i][j]=0;
            }
        }
        for(int i=data.stop_num+1; i<= 2*data.stop_num;++i){
            data.arcs[i][1+2*data.stop_num]=1;
        }
        data.arcs[0][1+2*data.stop_num]=1;
        //除去不符合车辆行驶最大限制的弧
        for(int i=0; i<2+2*data.stop_num; i++){
            for(int j=0;j<2+2*data.stop_num;j++){
                if(i != j){
                    if(data.car_time[i][j]>data.L)
                        data.arcs[i][j]=0;
                }
            }
        }

    }
    public static void main(String[] args) throws Exception {
        Data data = new Data();
        example.generate();
        String path = "data/test.txt";//算例地址
        proData(path,data);
        System.out.println("Input Successfully...");
        System.out.println("cplex procedure......");
        myhhctest cplex = new myhhctest(data);
        cplex.build_model();
        double cplex_time1 = System.nanoTime();
        cplex.solve_model();
        cplex.solution.fesible();
        double cplex_time2 = System.nanoTime();
        double cplex_time = (cplex_time2 - cplex_time1) / 1e9;//求解时间，单位s
        System.out.println("\nThe best cost:" + cplex.total_cost);
        System.out.println("cplex time " + cplex_time );
    }
}