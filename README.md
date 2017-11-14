# TransactionStatistics
Transaction Statistics
A Spring Boot application to adopt real time transaction updates.

The application has 2 end point:

  ### 1 Post /Transaction
  ### 2 Get  /Statistics
  
  Both the end points are non-blocking. Springs DefferedResult and Java's Native ExecutorService has been used to
  make it asynchronous and parallel thread capable at the same time.
  
  A Stress Test has also been added to simulate near real time scenario. Upto 10 Threads has been used to invoke the
  endpoints with 400 iterations. This was done to see if the end points funciton in an non-blocking under duress.
  This scenario has been created, to check if the endpoints are executing in constant time and memory




