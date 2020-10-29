##demo运行
    
    1.安装freeswitch，略，已经存在1000-1019这20个账户,如果不想安装，其实可以参考我的另一个项目 jain-sip-server,就是个sip的转接中心
    
    2.com.yiwise.voip
        AppA 和 AppB 模拟freeswitch的两个leg，配置项写死在代码里面
     
    3.运行启动AppA  和  AppB ,然后在A的控制台里面输入 call sip:1001@47.100.166.61:5060  回车，代表向B打电话
      这时候等控制台中出现 ring 日志，代表有来电，这时候，在B的控制台里面输入pickup接听电话，挂电话在A或者B的控制台
      中输入 hangup 回车
     
    4.代码来自  https://github.com/ymartineau/peers
  
    5.新增了VipDemoListener 这个类，通过mvn clean package 打包之后，启动jar包，可以注册sip账号 
      
      java -jar voip-demo-1.0-SNAPSHOT.jar 账号@sip服务器:端口 账号密码
      
      如：java -jar voip-demo-1.0-SNAPSHOT.jar 1000@47.99.84.2:5070 yiwise
      
      控制台会出现sip的日志，这时候可以在控制台里面输入 call sip:1001@47.100.166.61:5060 回车即可进行外呼