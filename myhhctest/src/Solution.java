import ilog.concert.IloException;
import java.util.ArrayList;

class Solution {
    double epsilon = 0.0001; //极小值
    public Data data = new Data();
    ArrayList<ArrayList<Integer>> routes = new ArrayList<>();//记录 每个车辆的路径
    double[] stop_times = new double [data.stop_num];//记录 每个停靠点完成服务需要的时间 范围D
    int[] stop_demands = new int [2+2*data.stop_num];//记录 每个点对应的负载值 范围V\C
    ArrayList<ArrayList<Double>> start_serve = new ArrayList<>();//记录 车的开始服务时间序列 范围V

    public Solution(Data data, ArrayList<ArrayList<Integer>> routes, ArrayList<ArrayList<Double>> start_serve, double[] stop_times,int[] stop_demands) {
        super();
        this.data = data;
        this.routes = routes;
        this.stop_times = stop_times;
        this.stop_demands = stop_demands;
        this.start_serve = start_serve;

    }
    public int double_compare(double v1,double v2) {//函数功能：比较两个数的大小
        if (v1 < v2 - epsilon)
            return -1;
        if (v1 > v2 + epsilon)
            return 1;
        return 0;
    }
    public void fesible() throws IloException {//函数功能：解的可行性判断
        //车辆数量可行性判断
        if (routes.size() > data.vec_num) {
            System.out.println("vehicle num error !!!");
            System.exit(0);
        }
        //车辆载荷可行性判断
        for (int k = 0; k < routes.size(); k++) {//对每一辆车
            ArrayList<Integer> k_route = routes.get(k);//找到该车的路径route
            double capasity = 0;
            //计算每条路径的需求量之和
            for (int i = 0; i < k_route.size(); i++) {
                capasity += this.stop_demands[k_route.get(i)];//车k经过的每个停靠点的需求总和
            }
            if (capasity > data.Q) {
                System.out.println("vehicle capacity error !!!");
                System.exit(0);
            }
        }
        //可行性判断：和约束条件的重复写。车辆行驶时间？

    }
}
