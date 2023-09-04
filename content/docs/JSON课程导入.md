---
title: 'JSON课程导入'
---

## 概述

Pure课程表支持直接导入已经处理好的课程信息文件  
用户可以直接将信息导入到Pure课程表中展示  

作为开发者，您可以选择将原始带有重复甚至是错误或缺失的信息交给Pure课程处理  
通过控制参数，您可以轻松做到合并重复的课程或者课程时间

## 适配

课程信息必须处理为Pure课程表可读的JSON格式  
要求见[课程导入JSON]({{< ref "课程导入JSON" >}})  

## 调用

在其他App中使用Intent隐式启动外部课程处理器进行课程导入，Intent要求如下：  

Action: tool.xfy9326.schedule.action.JSON_COURSE_IMPORT  
Type: application/json  
Data: 需要传递的JSON所在的URI，满足application/json的MIME
Extra:
| 名称                         | 类型    | 说明                                                                                                                                                                                                                 |
| ---------------------------- | ------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| COMBINE_COURSE               | Boolean | 是否需要合并相同的课程（可选）<br/>默认为false，开启后将会根据[课程]与[教师]合并相同的课程，课程时间将会去重合并                                                                                                     |
| COMBINE_COURSE_TIME          | Boolean | 是否需要合并相同的课程时间（可选）<br/>默认为false，开启后将会对同一个课程下的课程时间中的上课时间进行合并<br/>例如在[上课周数]，[星期]与[上课地点]相同的情况下：1-2，3-4，5，8，9，11节课将会合并为1-5，8-9，11节课 |
| COMBINE_COURSE_TEACHER       | Boolean | 合并相同的课程时，是否需要合并相同的教师（可选）<br/>默认为false，仅在COMBINE_COURSE为true时有效                                                                                                                     |
| COMBINE_COURSE_TIME_LOCATION | Boolean | 合并相同的课程时间时，是否需要合并相同的上课地点（可选）<br/>默认为false，仅在COMBINE_COURSE_TIME为true时有效                                                                                                        |

Flags: Intent.FLAG_GRANT_READ_URI_PERMISSION  （授予其他APP读取URI的权限）  

当且仅当课程导入成功时，返回RESULT_OK  

注1：之所以使用本地文件URI传递数据，是为了防止传递文件较大导致出错  
注2：您可以把需要解析的文本数据写入App内置存储（例如cache文件夹）后，使用[FileProvider](https://developer.android.com/training/secure-file-sharing/setup-sharing)生成URI传递给Pure课程表  
注3：为了防止未安装Pure课程表导致的错误，建议使用Intent.resolveActivity()方法先进行检测  
注4：安卓高版本可能要求在AndroidManifest中添加<queries>以控制[软件包可见性](https://developer.android.com/training/basics/intents/package-visibility)，请酌情处理

### 示例

``` kotlin
object PureScheduleApi {
    private const val ACTION_JSON_COURSE_IMPORT = "tool.xfy9326.schedule.action.JSON_COURSE_IMPORT"
    private const val EXTRA_COMBINE_COURSE = "COMBINE_COURSE"
    private const val EXTRA_COMBINE_COURSE_TIME = "COMBINE_COURSE_TIME"

    fun jsonCourseImport(context: Context, uri: Uri): Boolean {
        val intent = Intent(ACTION_JSON_COURSE_IMPORT).apply {
            setDataAndType(uri, "application/json")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(EXTRA_COMBINE_COURSE, false)
            putExtra(EXTRA_COMBINE_COURSE_TIME, false)
        }
        return if (intent.resolveActivity(context.packageManager) == null) {
            false
        } else {
            context.startActivity(intent)
            true
        }
    }
}
```

### 软件包可见性控制

``` xml
<queries>
    <intent>
        <action android:name="tool.xfy9326.schedule.action.JSON_COURSE_IMPORT" />
        <data android:mimeType="application/json" />
    </intent>
</queries>
```

### 常见问题

1. 如果提示‘启动参数有缺失’，请注意核对参数是否填写完整
2. 如果提示‘初始化错误’，可能是由于Pure课程表出现问题，请及时上报
3. 如果在导入课程时提示‘课程导入页面错误’，在确认传入数据无误后，有可能是URI读取权限问题。请先尝试在自己的App中读取这个URI的内容。由于安卓版本的原因，可能直接存放在files，cache等文件夹根目录的文件无法读取，建议使用一个文件夹例如cache/html存放文件，然后生成URI。
