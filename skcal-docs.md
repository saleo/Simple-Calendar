### skcal功能结构小记和指标、数据字典

商丘市寿康文化研究学会，组织机构代码号（统一代码、登记证号）51411400796793391R，商丘市八一路德信大厦912

### 功能

1. 设置》开启reminder：启动未来自定义和系统事件提醒
2. 设置》关闭reminder：取消未来自定义和系统事件提醒（此时events表内事件条目仍在）

>

*标示-e：可对外；i：不对外*



e:



结构：

MA：6个fragment，day，week，month，year，eventlist，qingxin

SA-setting

AA

AAHealth

AAIntro

AACredit

License.

### 数据字典：

1. ics文件中dtstart字段标示时间必须是000001-零点零分1秒，dtend字段标示001001零点10分1秒（后者不起作用，占位符）

2. 自定义事件，时长为以使用日开始的2年

3. SOURCE_CUSTOMIZE_ANNIVERSARY, events中source字段，标示是自定义事件

4. 目前涉及event表修改startTs的是addCustomizeEvent1个函数，context.fetchCalDavSync。

5. 涉及notification的共5个,均只处理近1个月内的notifications(太多担心alarm耗电厉害)：

   > settings中的reminderSwitch,reminderTime,addCustomizeEvent,3个开关
   >
   > bootComplete
   >
   > import-ics
   
6. 如遇当天有戒期的，则在提醒设置（调整）

7. 本以为要处理modifyCustomizeEvent之后的reminder，看之后发现：modify=delete+add,而这2者分别处理过了

8. 目前仅支持竖版：不会随屏幕横置而横向显示

9. 表events中reminder_minutes1,2,3均没用了，因为同一从config中取

10. calDav相关的也没用了，因为提醒数据从网上下载后导入本地表

11. 当前在用荣耀8对应android8.0，是从android7升级而来

12. 清心格言内置程序内

13. minSdk升级到19，考虑到现在少有机器还是在用android4.4，以及检测通知用的areNotificationsEnabled只支持19+。

---

包含的module：

1. icsDroid:目前跟skcal同一git控制
2. cert4android:单独git控制(submodule)
3. ical4android：单独git控制(submodule)

---

> -16776961
>
> -7829368
>
> -65536

### 使用说明：

如果重启手机，请尽快启动一次本程序、以激活通知功能（国内手机厂商限制所致）