/**
 * @author :KWANG
 * @description :
 * @create :2022-12-06 15:34:00
 */
class Data {
    int cus_num;          //客户点数目
    int stop_num;            //停靠点数目 不包括治疗中心
    int[][] cus_location;        //所有客户点的坐标x,y
    int[][] stop_location;   //停靠点和副本的坐标，包括治疗中心及其副本，0和1+2*stop_num是治疗中心
    //int[] cus_demands;          //客户点需要的治疗师人数  默认为1
    double[] cus_times;      //客户点的服务时间序列
    double T;               //治疗师在停靠点的最大等待时间
    int vec_num;            //车辆数目
    double Q;             //车辆容量
    double F;                  //车辆固定成本
    int[] vehicles;          //车辆编号
    double L;               //每个车辆最大行驶时间
    int[][] arcs;          //arcs[i][j]表示i到j点的弧
    double[][] car_time;       //车从i到j的时间，满足三角关系，表示花费
    double[][] human_time;     //在停靠点和客户点之间的，人行走的时间

    public double double_truncate(double v) { //截断小数3.26434-->3.2
        int iv = (int) v;
        if (iv + 1 - v <= 0.000000001)
            return iv + 1;
        double dv = (v - iv) * 10;
        int idv = (int) dv;
        double rv = iv + idv / 10.0;
        return rv;
    }
}
