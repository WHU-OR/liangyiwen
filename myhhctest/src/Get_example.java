import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import  java.util.Random;
import java.util.Scanner;

//从索罗门c101中随机生成算例
public class Get_example{
    Random random;
    public Get_example(){random = new Random();}
    public static boolean existed(int num, int[] luckNums, int index) {
        //检查抽取的随机数是否已存在
        for(int i=0; i<index; i++) {
            if(num == luckNums[i]) {
                return true;
            }
        }
        return false;
    }
    public int[] luckynum(int a,int b, int c){//返回a-b范围内不重复的c个随机数
        int[] luckNums = new int[c];
        for(int i=0; i<c; i++) {
            //在a到b之间取随机数
            int r= random.nextInt(b-a+1) + a;
            while(existed(r, luckNums, i)) {
                r= random.nextInt(b-a+1) + a;
            }
            luckNums[i] = r;
        }
        return luckNums;
    }
    public void generate() throws Exception{
        String read_path = "data/c101.txt";//基准算例
        String rline = null;
        Scanner read = new Scanner(new BufferedReader(new FileReader(read_path)));  //读取文件
        rline = read.nextLine();
        rline = read.nextLine();
        String[] substr = null;
        int[][] list= new int [101][4];
        for(int i=0;i<101;i++){//将c101所有点的数据存入list数组
            rline = read.nextLine();
            substr = rline.trim().split("\\s+");
            list[i][0] = Integer.parseInt(substr[0]);
            list[i][1] = Integer.parseInt(substr[1]);
            list[i][2] = Integer.parseInt(substr[2]);
            list[i][3] = Integer.parseInt(substr[3]);
        }
        read.close();//关闭流
        //随机生成算例
        int stop_num=6;
        int[][]stops = new int [stop_num+1][4];//记录所有停靠点（包括中心）
        stops[0] = list[0];//医疗中心点
        int cus_num = 0;//记录总的客户点数
        int[][]cus = new int [40][4];//记录所有客户点
        //1-11为第一类
        int n1=random.nextInt(4)+1;//随机生成1-5之间的数,选几个点
        int []num1=luckynum(1,11,n1);//n1个点的索引值
        int s=random.nextInt(n1)+1;//在n1个点里第s个作为停靠点
        stops[1]=list[num1[s-1]];
        for(int i=0;i<s-1;++i)
            cus[i+cus_num]=list[num1[i]];
        for(int i=s;i<n1;++i){
            cus[i+cus_num-1]=list[num1[i]];
        }
        cus_num=n1-1;
        int n2=random.nextInt(4)+1;//随机生成1-5之间的数,选几个点
        int []num2=luckynum(12,19,n2);
        s=random.nextInt(n2)+1;
        stops[2]=list[num2[s-1]];
        for(int i=0;i<s-1;++i)
            cus[i+cus_num]=list[num2[i]];
        for(int i=s;i<n2;++i){
            cus[i+cus_num-1]=list[num2[i]];
        }
        cus_num += (n2-1);
        int n3=random.nextInt(4)+1;//随机生成1-5之间的数,选几个点
        int []num3=luckynum(20,30,n3);
        s = random.nextInt(n3)+1;
        stops[3]=list[num3[s-1]];
        for(int i=0;i<s-1;++i)
            cus[i+cus_num]=list[num3[i]];
        for(int i=s;i<n3;++i){
            cus[i+cus_num-1]=list[num3[i]];
        }
        cus_num += (n3-1);
        int n4=random.nextInt(4)+1;//随机生成1-5之间的数,选几个点
        int []num4=luckynum(31,39,n4);
        s = random.nextInt(n4)+1;
        stops[4]=list[num4[s-1]];
        for(int i=0;i<s-1;++i)
            cus[i+cus_num]=list[num4[i]];
        for(int i=s;i<n4;++i)
            cus[i+cus_num-1]=list[num4[i]];
        cus_num += (n4-1);

        int n5=random.nextInt(4)+1;//随机生成1-5之间的数,选几个点
        int []num5=luckynum(40,52,n5);
        s = random.nextInt(n5)+1;
        stops[5]=list[num5[s-1]];
        for(int i=0;i<s-1;++i)
            cus[i+cus_num]=list[num5[i]];
        for(int i=s;i<n5;++i)
            cus[i+cus_num-1]=list[num5[i]];
        cus_num += (n5-1);

        int n6=random.nextInt(4)+1;//随机生成1-5之间的数,选几个点
        int []num6=luckynum(53,60,n6);
        s = random.nextInt(n6)+1;
        stops[6]=list[num6[s-1]];
        for(int i=0;i<s-1;++i)
            cus[i+cus_num]=list[num6[i]];
        for(int i=s;i<n6;++i)
            cus[i+cus_num-1]=list[num6[i]];
        cus_num += (n6-1);
        /*
        int n7=random.nextInt(4)+1;//随机生成1-5之间的数,选几个点
        int []num7=luckynum(61,71,n7);
        s = random.nextInt(n7)+1;
        stops[7]=list[num7[s-1]];
        for(int i=0;i<s-1;++i)
            cus[i+cus_num]=list[num7[i]];
        for(int i=s;i<n7;++i)
            cus[i+cus_num-1]=list[num7[i]];
        cus_num += (n7-1);

        int n8=random.nextInt(4)+1;//随机生成1-5之间的数,选几个点
        int []num8=luckynum(72,81,n8);
        s = random.nextInt(n8)+1;
        stops[8]=list[num8[s-1]];
        for(int i=0;i<s-1;++i)
            cus[i+cus_num]=list[num8[i]];
        for(int i=s;i<n8;++i)
            cus[i+cus_num-1]=list[num8[i]];
        cus_num += (n8-1);

        int n9=random.nextInt(4)+1;//随机生成1-5之间的数,选几个点
        int []num9=luckynum(82,91,n9);
        s = random.nextInt(n9)+1;
        stops[9]=list[num9[s-1]];
        for(int i=0;i<s-1;++i)
            cus[i+cus_num]=list[num9[i]];
        for(int i=s;i<n9;++i)
            cus[i+cus_num-1]=list[num9[i]];
        cus_num += (n9-1);

        int n10=random.nextInt(4)+1;//随机生成1-5之间的数,选几个点
        int []num10=luckynum(92,100,n10);
        s = random.nextInt(n10)+1;
        stops[10]=list[num10[s-1]];
        for(int i=0;i<s-1;++i)
            cus[i+cus_num]=list[num10[i]];
        for(int i=s;i<n10;++i)
            cus[i+cus_num-1]=list[num10[i]];
        cus_num += (n10-1);
*/
        for(int i=0;i<cus_num;i++){
            cus[i][0]=i;
        }
        for(int i=0;i<stop_num+1;i++){
            stops[i][0]=i;
        }
        //写入文件
        //String write_path = "data/test.txt";//写入文件
        //BufferedWriter write = new BufferedWriter(new FileWriter(write_path));
        PrintWriter write = new PrintWriter("data/test.txt");
        write.println("test");
        write.println(" ");
        write.println("cus_num  stop_num  vec_num   T   Q   F   L");
        write.println(cus_num+"  "+stop_num+"  "+(stop_num+1)+"  30  6  200  300");
        write.println("CUSTOMERS");
        write.println(" NO.   X.   Y.   SERVETIME");
        for(int i=0;i<cus_num;i++){
            write.println(cus[i][0]+"  "+cus[i][1]+"  "+cus[i][2]+"  "+cus[i][3]);
        }
        write.println("STOPS (0 is the center)");
        write.println(" NO.  X.   Y.  ");
        for(int i=0;i<stop_num+1;++i){
            write.println(stops[i][0]+"  "+stops[i][1]+"  "+stops[i][2]);
        }
        write.close();
    }

}
