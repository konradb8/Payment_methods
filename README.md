# Payment methods app

#### Application calculates optimal payment methods for given orders.


`Maven 3.5.0`

`Java 21`

### Build

 ```sh
 mvn clean package
 ```

In `/target` an `app.jar` should be present.

### Run

```sh
java -jar target/app.jar orders.json paymentmethods.json
```