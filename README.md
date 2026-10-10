# auto-validate
Library for creating self-validating objects in Java

# So, what's the deal?
Consider this simple Java bean:
```java
class DemoBean {
    @Size(min = 1, max = 256)
    private String firstName;

    String getFirstName() {
        return firstName;
    }

    void setFirstName(final String firstName) {
        this.firstName = firstName;
    }


    @NotNull
    @Size(min = 1, max = 256)
    private String lastName;


    String getLastName() {
        return lastName;
    }

    void setLastName(final String lastName) {
        this.lastName = lastName;
    }


    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")
    private String email;

    String getEmail() {
        return email;
    }

    void setEmail(final String email) {
        this.email = email;
    }
}      
```

or rather the way more concise [Lombok-ified](https://objectcomputing.com/resources/publications/sett/january-2010-reducing-boilerplate-code-with-project-lombok) equivalent:  
```java
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
class DemoBean {

    @Size(min = 1, max = 256)
    String firstName;

    @NotNull
    @Size(min = 1, max = 256)
    String lastName;

    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")
    String email;
}
```


This class clearly declares validation rules for ensuring the integrity of any class instance.
But what does that mean, actually?
Let's write a test and find out:
```java
DemoBean demo = new DemoBean();         // invalid!
demo.setFirstName("Joey");              // invalid!
demo.setLastName("Ramone");             // invalid!
demo.setEmail("joey_at_ramones.com");   // invalid!
```
At any given time the object has been invalid.
Let's create the first valid version:
```java
demo.setEmail("joey@ramones.com");  // de-facto valid...
```
At this point the object is de-facto valid but this fact has not been checked yet.
For this to be done some code OUTSIDE of the object needs to be executed, e.g.:
```java
ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
Validator validator = validatorFactory.getValidator();
Set<ConstraintViolation<DemoBean>> violations = validator.validate(demo);
assertEquals(0, violations.size(), "Object should be valid.");
```

This means the poor object may declare rules of its validity as much as it wants - but it has no control over it.

But it gets worse: Now that the object is in a valid state at last, let's break it again:
```java
demo.setEmail("This is not a valid email address!");
violations = validator.validate(demo);
assertEquals(1, violations.size(), "Object should have 1 error because the email address is wrong.");
```
Oops, a simple, maybe accidental, call of a setter broke the object's validity and no-one ever noticed,
neither the code that asserted the object's validity a second ago nor the object itself.
In order to find out we had to call the validate method again.


# Some remedy: Builders
We can solve the second problem by using the Builder pattern.
With Lombok, this is actually [super-easy, barely an inconvenience](https://twitter.com/hashtag/SuperEasyBarelyAnInconvenience?src=hash):
```java
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Builder(toBuilder=true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class DemoBuilder {

    @Size(min = 1, max = 256)
    String firstName;

    @NotNull
    @Size(min = 1, max = 256)
    String lastName;

    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")
    String email;
}
```

Now there are no more setters and each field is final.
This baby is immutable now.
In order to change a value we would need to "clone" it first.
To achieve this, we can use the toBuilder method that Lombok generated for us (@Builder(toBuilder=true)).
```java
final DemoBuilder demo2 = demo.toBuilder()
                .email("Some broken email address")
                .build();                           
assertNotSame(demo, demo2, "The clone must not be the same instance as the original object.");

violations = validate(demo); 
assertEquals(0, violations.size(), "Original object should still be valid.");     

violations = validate(demo2); 
assertEquals(1, violations.size(), "Object 2 should have 1 error because the email address is wrong.");
```


However, the first problem remains:
```java
DemoBuilder demo = DemoBuilder.builder()        // not done yet
        .firstName("Joey")                      // not done yet
        .lastName("Ramone")                     // not done yet
        .email("Totally broken email address")  // not done yet
        .build();                               // INVALID 
```        
At this point the object is INVALID because the email address is broken.
However, the object doesn't know that it's broken.
Any code that uses the object does not know the object is broken and consists of invalid data.
We still depend on a mechanism outside of the object to call the validate method.


# Enter AutoValidate
Here's a solution to both problems that this "auto-validate" library provides:
1. An object that is guaranteed to be either valid or non-existent (by throwing an exception).
2. ... and immutable.
```java
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Builder(toBuilder=true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@AutoValidate
class DemoAutovalidateClass {

    @Size(min = 1, max = 256)
    String firstName;

    @NotNull
    @Size(min = 1, max = 256)
    String lastName;

    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")
    String email;
}
```

Um, how is this different?
Well, the magic lies in the @AutoValidate annotation.

Here's how you can use (and test) it:
```java
@Test
public void test_demo() {
    final DemoAutovalidateClass demo = DemoAutovalidateClass.builder()    // not done yet
            .firstName("Joey")                                      // not done yet
            .lastName("Ramone")                                     // not done yet
            .email("joey@ramones.com")                              // not done yet
            .build();
    // If we are still "alive" at this point then the object MUST be valid.

    // Fortunately, making it INVALID again is not possible...
    // Let's "clone" it instead (using the toBuilder method provided by Lombok):
    try {
        DemoAutovalidateClass demo2 = demo.toBuilder()
                .email("joey_at_ramones.com")
                .build();
        fail("This should have failed because the object is not valid");
        // also: the variable demo2 was never assigned any value. 
        // The data was broken, so it never existed,
    }
    catch (ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        assertEquals(1, violations.size(), "Object should have 1 error because the email address is wrong.");
    }

    // At no point in the process has the object been available in an invalid state.
    // This means, if it exists it must be valid and can be trusted anywhere it is used.
}
```


# How it works
IN THE BACKGROUND, there's an AspectJ Pointcut for every constructor of a class annotated with @AutoValidate.
(Alternatively, you can annotate a specific constructor with the same annotation.)
For this Pointcut there's an AOP Advice that calls jakarta.validation for you right after the constructor has finished
but before any other code can use the object.
If the object is invalid then the Advice throws a jakarta.validation.ConstraintViolationException.

All YOU have to do:
1. Use the @AutoValidate annotation on each class you want to use this feature for, either on the level of the class or the constructor.
2. Enable AspectJ (see below).


# Enabling AspectJ
The aspect is applied by *load-time weaving*: the AspectJ weaver runs as a Java agent and
instruments your classes while the JVM loads them. No special compiler is needed.
There are two parts to it:

1. The auto-validate jar ships a `META-INF/aop.xml` that registers the `AutoValidateAspect`.
   The weaver finds this file on the classpath by itself, you don't have to configure anything.
2. You start the JVM with the weaver as agent:
   ```
   java -javaagent:/path/to/aspectjweaver-1.9.25.1.jar -jar your-app.jar
   ```
   `aspectjweaver` is a transitive dependency of auto-validate, so the jar is already in your local Maven repository at
   `~/.m2/repository/org/aspectj/aspectjweaver/<version>/aspectjweaver-<version>.jar`.

Without the agent nothing fails - the annotation is simply ignored and your objects are NOT validated.
So make sure there's at least one test that expects a ConstraintViolationException.

For tests run by Maven, pass the agent to Surefire. The `properties` goal of the dependency plugin
provides the path of the weaver jar, so it doesn't have to be hard-coded:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>properties</goal>
            </goals>
        </execution>
    </executions>
</plugin>
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>-javaagent:${org.aspectj:aspectjweaver:jar}</argLine>
    </configuration>
</plugin>
```
In an IDE, add the same `-javaagent:...` option to the VM options of your run configuration.


# Building
Requires Java 21 or newer and Maven.
```
mvn test
```
runs the tests of the library (`autovalidate`) and of the Spring Boot example application (`rest-example`).
