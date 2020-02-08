import csv
from random import randrange, random

num_of_queries = 20
output_file = "query2.csv"


queries = []

for i in range(num_of_queries): 
  query = []
  direction = "inbound" if randrange(2) == 0 else "outbound"
  protocol = "tcp" if randrange(2) == 0 else "udp"

  port = randrange(1,65536)
  
  ip = []
  for i in range(4):
    ip.append(randrange(256))
  ip = ".".join([str(x) for x in ip])


  query.append(direction)
  query.append(protocol)
  query.append(port)
  query.append(ip)

  queries.append(query)







with open(output_file, "wb") as csv_file:
  writer = csv.writer(csv_file, delimiter=' ')
  writer.writerow([num_of_queries])
  for query in queries:
    writer.writerow(query)

