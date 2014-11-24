# _Openkitchen Akka Event Sourcing Application_ 

## Application
The application is a simple webshop that is build with the following stack: 
- Akka
- Spray
- AngularJS
- Twitter Bootstrap

## Build and Run
### With Typesafe Activator
- Execute ```./bin/activator ui```
- Activator is a web-ide. Write code, compile it, run tests, run the application or generate files for your favourite IDE.
- To start the application click on _Run_ and then _Start_ 
- Open a browser using the following url: ```http://localhost:8080/```
- From there all endpoints of the REST interface can be called using the gui

### With SBT  
- Execute ```./bin/sbt.sh```  
- Type ```run```
- Open a browser using the following url: ```http://localhost:8080/```
- From there all endpoints of the REST interface can be called using the gui

## Create Executable jar
- Execute ```./bin/sbt.sh assembly```. This will create an executable jar in ```target/scala-2.11```
- Run the server as follows: ```java -jar xita-innovationday-assembly-<version>.jar```


## Test REST API manually 
_Note:_ Since it's a shopping cart the REST API is session-based. The name of the session cookie is *session-id*.

- Add an item to the shopping cart:
```
curl -b session-id=12121212 -d '{"itemId":"dell-venue"}' -H "Content-Type: application/json"  http:localhost:8080/cart
```

- Retrieve shopping cart contents:
```
curl -b session-id=12121212 http://localhost:8080/cart
```

- Remove an item from the shopping cart:
```
curl -b session-id=12121212 -X "DELETE" http://localhost:8080/cart?itemId=dell-venue
```

- Place order
```
curl -b session-id=12121212 -X "PUT" http://localhost:8080/order
```

##Labs
###Lab 1: ```git checkout lab1```
- Starting point: Web UI & Spray Rest API voor shopping cart manipulations. The Cart*s* Manager Actor and Cart Actor are not implemented
- Lab 1: Implement the ```com.xebia.openkitchen.cart.CartManagerActor``` that handles ```com.xebia.openkitchen.cart.CartManagerActor.Envelope``` messages received from the Spray route: ```com.xebia.openkitchen.api.Api```. Per session the ```CartManagerActor``` has to create a Cart Actor. To achieve that use the ```Envelope```'s session id to create or get a Cart Actor using the Props passed as a constructor argument. The payload of the ```Envelope``` needs to be forwarded to the Cart Actor. As for now use a Dummy implementation for the Cart Actor. 
- Make the following test succeed: ```com.xebia.openkitchen.cart.CartMangerActorSpec``` 

###Lab 2 ```git checkout lab2```
- Starting point: Implementation of the Cart*s* Manager Actor (```CartManagerActor```). The Cart Actor is not implemented.
- Lab 2: Implement the ```com.xebia.openkitchen.cart.SimpleCartActor``` that keeps the cart data in-memory. To see which operations this Actor must support take a look at the Spray route: ```com.xebia.openkitchen.api.Api```, which sends the payload intended for the Cart Actor wrapped in an ```Envelope```. As mentioned in the previous lab the Cart Actor only has to handle the payload message (like ```com.xebia.openkitchen.cart.CartDomain.AddToCartRequest, RemoveFromCartRequest, GetCartRequest, OrderRequest```) and not the whole ```Envelope``` message.
- Make the following test succeed: ```com.xebia.openkitchen.cart.SimpleCartActorSpec```  

###Lab 3 ```git checkout lab3```
- Starting point: Implementation of an in-memory Cart Actor (```SimpleCartActor```)
- Lab 3: Implement the ```com.xebia.openkitchen.cart.PersistentCartActor```, which makes use of Event Sourcing (Akka persistence) to persist all cart events. Change the Props passed to the ```CartManagerActor``` in ```com.xebia.openkitchen.api.WebshopActor ``` to return an instance of ```PersistentCartActor```.
- Make the following test succeed: ```com.xebia.openkitchen.cart.PersistentCartActorSpec```

###Bonus Lab 4 ```git checkout lab4```
- Starting point: Implementation of a persistent Cart Actor (```PersistentCartActor```)
- Lab 4: Extend the ```com.xebia.openkitchen.cart.PersistentCartActor``` to make use of: _passivation_ and _snapshotting_
- Implement a receiveTimeout (e.g. 10 seconds). Upon receival of the ```akka.actor.ReceiveTimeout``` message take a snapshot (```saveSnapshot```) of the current cart
- After the snapshot is saved Akka persistence sends a  ```akka.persistence.SaveSnapshotSuccess``` or ```akka.persistence.SaveSnapshotFailure``` message to the actor. In either case kill the actor
- When the actor recovers ```receiveRecover``` receives  a ```akka.persistence.SnapshotOffer```. Make sure it is processsed correctly 

###Solution ```git checkout final-solution```
- Final solution with the ```com.xebia.openkitchen.cart.PersistentCartActor``` that supports snapshotting en passivation


