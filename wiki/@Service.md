## @Service

### Step 1

Create interface `UserService.java`

```java
public interface UserService {
    void create();
}
```

### Step 2

Create class `UserMockServiceImpl.java` add @Service

```java
@Service
public class UserMockServiceImpl implements UserService {


    @Autowired
    private Eorm eorm;


    @Transaction
    public void create() {

        User user = new User();
        user.setName("fixedRate");
        user.setMobile("10000000L");
        eorm.insert(user);
    }

}
```


#### Tip

+ @Service 注解必须是实现接口的类中使用，因为使用的是JDK Proxy实现