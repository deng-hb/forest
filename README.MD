## Forest

![forest](logo.png)

Simple RESTFul Server

```
<dependency>
    <groupId>com.denghb</groupId>
    <artifactId>forest</artifactId>
    <version>1.0</version>
</dependency>
```

### Example

```
import com.denghb.restful.Application;
import com.denghb.restful.annotation.GET;
import com.denghb.restful.annotation.RESTful;


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

Run `App.java` Open Browser [http://localhost:8888](http://localhost:8888)



more [simple-restful-server](https://github.com/deng-hb/simple-restful-server)
