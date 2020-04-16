# Cerberus

受疫情影响现在家远程办公，出于某种特殊原因，需要做一个监控电脑文件变化的小工具

技术栈：
* springboot 2.1.3.RELEASE
* gradle 5.2.1
* jdk 1.8(1.7+ required)

## 使用说明
配置监控路径的两种方式，优先启动参数
* application.yml配置监控文件路径和输出文件路径
* 启动参数中加入--file.watch.path= --file.copy.path=

## 2020.04.16 V1
实现了基础功能，对于被监控文件夹中文件更新行为进行监控，更新后可拷贝文件到指定路径
