---
title: '课程导入JSON'
---

## 概述

该JSON对象用于课程导入  

## JSON模版

``` json
{
  "version": 1,
  "times": [
    { "start": "08:00", "end": "08:40" },
    { "start": "08:50", "end": "09:30" }
  ],
  "courses": [
    {
      "name": "",
      "teacher": "",
      "times": [
        {
          "weekNum": [ 1, 2, 3, 7, 9 ],
          "weekDay": 1,
          "start": 1,
          "duration": 1,
          "sections": [ 1, 2, 3 ],
          "location": ""
        }
      ]
    }
  ],
  "termStart": "2000-01-01",
  "termEnd": "2000-10-10"
}
```

## 字段解释

| 名称                   | 解释                                                                                                               |
| ---------------------- | ------------------------------------------------------------------------------------------------------------------ |
| version                | 导入课程格式版本，目前默认填写1                                                                                    |
| times                  | 课表时间数组                                                                                                       |
| times.start            | 课程开始时间<br/>HH:mm的24小时格式                                                                                 |
| times.end              | 课程结束时间<br/>HH:mm的24小时格式                                                                                 |
| courses                | 课程信息数组                                                                                                       |
| courses.name           | 课程名称                                                                                                           |
| courses.teacher        | 课程教师（该字段可以不存在）                                                                                       |
| courses.times          | 上课时间                                                                                                           |
| courses.times.weekNum  | 上课周数 从1开始                                                                                                   |
| courses.times.weekDay  | 上课星期 1～7 周一～周日                                                                                           |
| courses.times.start    | 课程开始节数 从1开始 （限制性可选，详见下方‘[课程节数的多种表达方式]({{< ref "#课程节数的多种表达方式" >}})’描述） |
| courses.times.duration | 课程时长 大于等于1 （限制性可选，详见下方‘[课程节数的多种表达方式]({{< ref "#课程节数的多种表达方式" >}})’描述）   |
| courses.times.sections | 课程节数数组 从1开始 （限制性可选，详见下方‘[课程节数的多种表达方式]({{< ref "#课程节数的多种表达方式" >}})’描述） |
| courses.times.location | 上课地点（该字段可以不存在）                                                                                       |
| termStart              | 学期开始时间（该字段可以不存在）<br/>格式：yyyy-MM-dd 例如：2000-01-01                                             |
| termEnd                | 学期结束时间（该字段可以不存在）<br/>格式：yyyy-MM-dd 例如：2000-10-10                                             |

## 课程节数的多种表达方式

JSON中Pure课程表支持两种课程节数的表示方式，即start - duration与sections  

* 若start与duration至少有任意一个为空，sections为空，则报错
* 若start与duration至少有任意一个为空，sections不为空，则使用sections中的数据
* 若start与duration不为空，sections为空，则使用start与duration中的数据
* 若start与duration不为空，sections也不为空，则优先使用start与duration中的数据

建议使用start - duration的表达方式，因为Pure课程表内部采用这种方式存储数据  
若使用sections表达方式，则Pure课程表有可能会重新分割上课时间  
若您可以接受重新分割上课时间，则两种表达方式都可以  

注：在课程或者课程时间有可能重复时，建议在导入方式提供的参数中启用combineCourse与combineCourseTime参数以重新整理课程与上课时间
