# cs290bHw4
CS 190B: Homework 4

Although this solution meets the requirements of the assignment, it is less than stellar in a few respects:

1. The API for Space-Callable tasks is essentially nonexistent. Rather, all composition tasks (i.e., instantiations of the TaskCompose class) are Space-Callable. This approach is not as desirable as one that allows the application programmer to mark all and only those tasks that are appropriate for that application to be commpleted on the Space.  It also does not allow the decision to complete a task on on the Space to be decided at runtime.  Both of these things might be easily accommodated by having a Boolean method isSpaceCallable() whose default implementation returns this instanceof SpaceCallable; but which can be overridden as desired by the application programmer.

2. The approach to ameliorate communication taken was perhaps the simplest possible: The ComputerProxy constructs and starts 2 threads for each available processor on its associated remote Computer.  These threads each are executing the remote method execute( Task ). Multiple ComputerProxy threads for each available processor on each Computer causes the Space to be less scalable than otherwise would be the case.  It also would be more satisfying to have a each Computer maintain a local task queue and result queue and have either the Computer or the ComputerProxy keep the local task queue nonempty with best effort and also keep the local result queue empty (i.e., all results returned to the ComputerProxy) with best effort.
