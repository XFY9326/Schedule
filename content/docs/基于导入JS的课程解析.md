---
title: '基于导入JS的课程解析'
---

## 概述

Pure课程表可以通过导入外部JS在浏览器中执行的方式添加课程导入选项  
但是需要注意的是，从第三方导入JS进行注入的方式可能会导致用户信息安全相关的问题  
  
目前支持两种JS接口：  

1. 小爱课表（可以快速接入）
2. Pure课程表（支持更多特性）

注：由于课程获取与解析都是在浏览器中完成的，因此自由度会更高。  

关于调试：[WebView网页调试]({{< ref "注意事项#WebView网页调试" >}})

## 接入方法

### 使用方法

在‘课程导入’中点击‘+’按钮，将配置文件的JSON的网络地址填入即可（也可以选择文件导入）  

Pure课程表也支持URI导入：`pusc://course_import/js_config?src=<JSON配置文件URL>`  
在浏览器的网页中打开这个链接就可以跳转到Pure课程表的JSON配置文件导入页面  
请注意，在浏览器的地址栏输入这个地址是无效的，必须使用网页的链接跳转才能被识别到  

例如：
```html
<a href="pusc://course_import/js_config?src=https://www.example.com/config.json">添加课程导入</a>
```

{{< rawhtml >}}
<div>
    <a href="pusc://course_import/js_config?src=https://www.example.com/config.json">添加课程导入</a>
</div>
{{< /rawhtml >}}


*注：在安装了Pure课程表的手机浏览器中点击此链接才有效果*

### 配置文件

用户导入支持从本地选择或者联网下载，而配置文件用于管理该JS导入方式的所有属性  
用户只需要导入配置文件就可以了  
注：配置文件是基于json的  

``` json
{
    "uuid": "",
    "config": 3,
    "version": 1,
    "schoolName": "",
    "authorName": "",
    "systemName": "",
    "jsType": "",
    "initPageUrl": "",
    "dependenciesJSUrls": [
        "https://code.jquery.com/jquery-3.7.0.slim.min.js"
    ],
    "providerJSUrl": "",
    "parserJSUrl": "",
    "updateUrl": "",
    "sortingBasis": "",
    "requireNetwork": false,
    "combineCourse": false,
    "combineCourseTime": false,
    "combineCourseTeacher": false,
    "combineCourseTimeLocation": false,
    "enableAsyncEnvironment": true
}
```

### 字段解释

| 名称                      | 解释                                                                                                                                                                                                                             |
| ------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| uuid                      | 标准的UUID格式，不区分大小写<br/>用于唯一标识一个配置文件                                                                                                                                                                        |
| config                    | 配置文件格式版本，详见[配置版本更新]({{< ref "#配置版本更新" >}})                                                                                                                                                                |
| version                   | 当前配置文件版本，更新判断是通过线上与线下配置文件不同来判断的，因此并不会判断version实际大小，修改该属性只是为了让用户收到更新<br/>修改配置文件其他属性同样可以触发更新                                                         |
| schoolName                | 学校名称                                                                                                                                                                                                                         |
| authorName                | 作者名称                                                                                                                                                                                                                         |
| systemName                | （教务）系统名称                                                                                                                                                                                                                 |
| jsType                    | JS接口类型<br/>目前只支持 AiSchedule 与 PureSchedule                                                                                                                                                                             |
| initPageUrl               | WebView初始显示的URL                                                                                                                                                                                                             |
| dependenciesJSUrls        | 依赖，运行环境需要的JS<br/>根据需要添加，该字段可以不存在，或者为[]                                                                                                                                                              |
| providerJSUrl             | 课程提供器下载地址                                                                                                                                                                                                               |
| parserJSUrl               | 课程解析器下载地址                                                                                                                                                                                                               |
| updateUrl                 | 更新地址，即本配置文件的下载地址                                                                                                                                                                                                 |
| sortingBasis              | 排序规则，将会影响该配置文件在列表中显示的位置<br/>在线课程导入列表将为依据此进行排序<br/>请注意，此处字符串中应该全部为ASCII码支持的字符且不包含空格，大小写字母不会影响排序<br/>国内院校可以使用拼音作为排序依据               |
| requireNetwork            | 是否需要联网操作（该字段可以不存在）<br/>默认为false，此功能不建议开启，详见底部[用户隐私安全]({{< ref "#用户隐私安全" >}})                                                                                                      |
| combineCourse             | 是否需要合并相同的课程（该字段可以不存在）<br/>默认为false，开启后将会根据[课程]与[教师]合并相同的课程，课程时间将会去重合并                                                                                                     |
| combineCourseTime         | 是否需要合并相同的课程时间（该字段可以不存在）<br/>默认为false，开启后将会对同一个课程下的课程时间中的上课时间进行合并<br/>例如在[上课周数]，[星期]与[上课地点]相同的情况下：1-2，3-4，5，8，9，11节课将会合并为1-5，8-9，11节课 |
| combineCourseTeacher      | 合并相同的课程时，是否需要合并相同的教师（该字段可以不存在）<br/>默认为false，仅在combineCourse为true时有效                                                                                                                      |
| combineCourseTimeLocation | 合并相同的课程时间时，是否需要合并相同的上课地点（该字段可以不存在）<br/>默认为false，仅在combineCourseTime为true时有效                                                                                                          |
| enableAsyncEnvironment    | 启用外部执行时的async同步函数环境<br/>默认为true，此功能不建议关闭除非用户手机的WebView版本过低                                                                                                                                  |

### 配置版本更新

当配置版本大于App内置支持的JS配置版本时会自动弹出错误  
高版本的App中兼容旧版本的JS配置版本，使用旧版本的JS配置版本时依然可以添加新增属性  
JS配置版本的主要作用是防止用户使用旧版本的App导致新的配置无法生效  

| 配置版本 | App版本       | 更新日志                                                                                     |
| -------- | ------------- | -------------------------------------------------------------------------------------------- |
| 1        | >= 1.0 (0)    | 初始版本                                                                                     |
| 2        | >= 1.4.9 (38) | 默认支持并使用async同步函数环境 <br/> 在小爱课表JS接口支持Pure课程表的特殊属性`pureSchedule` |
| 3        | >= 1.5.0 (40) | 支持combineCourseTeacher和combineCourseTimeLocation的配置项                                  |

### 小爱课表JS接口

注1：方法名与参数名必须一致，更多适配文档请查阅[小爱课程表官方文档]({{< ref "https://open-schedule-prod.ai.xiaomi.com/docs/#/help/" >}})，此处仅列出适配的接口  
注2：不支持在配置文件中设置调用Timer函数，但是可以在适配器或者解析器主动调用后返回结果

``` javascript
// 适配器接口
// 返回HTML字符串
function scheduleHtmlProvider(iframeContent = "", frameContent = "", dom = document)

// 或者使用同步函数（必须启用同步函数环境，默认启用）
async function scheduleHtmlProvider(iframeContent = "", frameContent = "", dom = document)
```

``` javascript
// 解析器接口
// 返回JS的课程信息对象（详见'小爱课表课程信息对象'）
function scheduleHtmlParser(html = "")

// Pure课程表支持使用同步函数，但是小爱课表不支持（必须启用同步函数环境，默认启用）
async function scheduleHtmlParser(html = "")
```

#### 小爱课表课程信息对象

旧版本的小爱课表解析器接口中使用如下结构返回信息：

```javascript
{
    courseInfos: [
        {
            name: "数学",
            position: "教学楼1",
            teacher: "张三",
            weeks: [1, 2, 3, 4],
            day: 3,
            sections: [1, 2, 3]
        }
    ],
    sectionTimes: [
        {
            section: 1,
            startTime: "08:00",
            endTime: "09:40"
        }
    ]
}
```

新的解析器接口返回的信息中的`sectionTimes`被转移到了Timer (`scheduleTimer`)中  
但是目前Pure课程表为了保证兼容性，并没有打算直接支持Timer (`scheduleTimer`)函数  
所以如果需要返回上课的时间信息请依然通过解析器接口返回（必须在此处返回`sectionTimes`属性，可以自己主动调用后返回结果）  

除此之外位置能够对Timer中的学期时间提供支持，Pure课程表新增了一个属性`pureSchedule`（可选属性）：

```javascript
{
    courseInfos: [],
    sectionTimes: [],
    pureSchedule: {
        termStart: "2000-01-01",
        termEnd: "2000-10-10"
    }
}
```

只需要提供学期开始和结束的时间，Pure课程表会自动根据当前时间进行计算  

当`termStart`不为空，但`termEnd`为空时，将会把学期的开始和结束时间设置为同一天  
当`termStart`为空，但`termEnd`不为空时，等同于都为空  
当`termStart`与`termEnd`都为空时，则会根据当前日期添加默认的学期开始和结束时间  


#### 关于浏览器环境的适配

由于小爱课程表自定义了部分浏览器函数，并且代码的执行环境并不都在浏览器中，所以需要补充一些依赖  
这些依赖的下载地址可以填充在`dependenciesJSUrls`中  

1. 由于小爱课表的解析器在服务器端Cheerio环境下运行，因此如果使用了`$`等非JS内置函数，需要补充加上Jquery依赖，此处建议使用slim.min版本减少下载体积  
2. 如果使用了AIScheduleTools弹窗，请添加依赖[AIScheduleTools.js](https://open-schedule-prod.ai.xiaomi.com/docs/code/AIScheduleTools.js)

注：请不要使用`await loadTool('AIScheduleTools')`等未定义的函数，而是直接导入需要的依赖  

*其他解决方案：自定义一个`async function loadTool(toolName)`函数*  

### Pure课程表JS接口

``` javascript
// 适配器接口
// 参数 字符串 字符串数组 字符串数组
// 返回HTML字符串（或者任意JS对象，将会传参到pureScheduleParser中）
function pureScheduleProvider(htmlContent = "", iframeContent = [], frameContent = [])

// 或者使用同步函数（必须启用同步函数环境，默认启用）
async function pureScheduleProvider(htmlContent = "", iframeContent = [], frameContent = [])
```

``` javascript
// 解析器接口
// 参数 适配器返回的数据
// 返回Pure课程表JSON信息对象，即‘课程导入JSON’
function pureScheduleParser(html = "")

// 或者使用同步函数（必须启用同步函数环境，默认启用）
async function pureScheduleParser(html = "")
```

相关：[课程导入JSON]({{< ref "课程导入JSON" >}})

### Demo参考  

详见 [NAUAISchedule](https://gitee.com/XFY9326/NAUAISchedule)

### 其他

Pure课程表会在同一个浏览器的JS环境下顺序执行适配器和解析器的代码，所以请保证两个函数的变量不会相互干扰

### 用户隐私安全

默认情况下JS脚本在浏览器中执行时是不启用网络环境的，只能够运行本地的脚本  
脚本运行结束后将会自动刷新界面  

在JS脚本中进行需要联网操作是不被建议，因为这有可能会损害用户的信息安全  
如果执意要开启联网JS脚本环境，请在”设置——在线课程导入“中开启功能
