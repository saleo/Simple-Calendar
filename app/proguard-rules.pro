-keep class net.euse.calendar.models.** { *; }
#基线包使用，生成mapping.txt
-printmapping mapping.txt
#生成的mapping.txt在app/build/outputs/mapping/release路径下，移动到/app路径下
#修复后的项目使用，保证混淆结果一致
#-applymapping mapping.txt
#hotfix
-keep class com.taobao.sophix.**{*;}
-keep class com.ta.utdid2.device.**{*;}
-dontwarn com.alibaba.sdk.android.utils.**
#防止inline
-dontoptimize

-keepclassmembers class net.euse.calendar.App {
    public *;
}
# 如果不使用android.support.annotation.Keep则需加上此行
#-keep class com.my.pkg.SophixStubApplication$RealApplicationStub
