# Toolkit-SCGP
WeBank Blockchain toolkit of Solidity Compiler Gradle Plugin

# 介绍

在原先基于控制台的编译方式下，用户不仅需要安装控制台这一组件，编译流程也相对繁复，需要拷贝solidity文件到控制台，编译合约后，还需要将java合约拷贝到本地。但如果采用合约编译插件，在完成轻量级的一次性配置后，用户只需在项目工程下运行该插件，插件即会自动从合约目录读取solidity文件并编译为java合约，并会自动拷贝到业务工程下的对应包中。整个过程既不需要安装控制台，也省去了拷贝动作。流程对比如下：


# 如何使用

见[文档](https://toolkit-doc.readthedocs.io/zh_CN/latest/docs/WeBankBlockchain-Toolkit-SCGP/index.html)