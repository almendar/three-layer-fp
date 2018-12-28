# three-layer-fp

## realworld layer 
Everything that one need to talk to but it's awful and it has nothing to do with logic of application.

Examples: 
- Object storage like S3
- Database like Postgres
- HTTP calls
- Other stores like Redis
- Logging facilities
- Metrics infrastructure 
- Payment processor like PayPal


## domain layer 
Everything that achieves some effect by altering or reading the outside world but in context of our domain

Examples:
- You need list of clients from some store. Underlying database is hidden you only have functions like 

```scala
def getClient(clientId:Id): F[Client]
``` 

- You need to store your ML algo to S3 store. S3 store is hidden you only see a function like: 

```scala
def storeModel(model:MLModel): F[Unit]
``` 

Sequencing happens based on F having a `Monad` instance.


## businesslogic
Pure logic of how we want to orchiestrate out application. There should be no effectful function here. 
One can use types like `Option` and `Either` but not `IO`, `Future`. All values that needs to be read from the outside 
world shoule alreadu be obtained in the lower level layers.