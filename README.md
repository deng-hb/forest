## Forest

![forest](logo.png)

Simple RESTFul Server
这是一个新物种包内置 socket通讯、HTTP解析、类似Spring Boot的功能、简易JSON解析器、关系型数据库ORM等，使其能用最简洁的代码实现RESTful风格提供服务（暂仅供学习交流）

### Example

Create Maven "maven-archetype-quickstart" Project

Add

```
<dependency>
    <groupId>com.denghb</groupId>
    <artifactId>forest</artifactId>
    <version>1.0.1</version>
</dependency>
```


Create `App.java`

```
import com.denghb.forest.Application;
import com.denghb.forest.annotation.GET;
import com.denghb.forest.annotation.RESTful;


@RESTful
public class App {

     public static void main(String[] args) {
         Application.run(App.class, args);
     }

    @GET("/")
    String home() {
        return "Hello World!";
    }
}
 ```

Main Run `App.java` Open Browser [http://localhost:8888](http://localhost:8888)


👏意见反馈 [issues](https://github.com/deng-hb/forest/issues)

QQ群：701075954

