import csv
from random import randrange, random

num_of_entries = 10000000
p_port_range = 0.2
p_ip_range = 0.2
output_file = "test3.csv"


entries = []

for i in range(num_of_entries): 
  entry = []
  direction = "inbound" if randrange(2) == 0 else "outbound"
  protocol = "tcp" if randrange(2) == 0 else "udp"

  port_start = randrange(1,65536)
  if (random() < p_port_range):
    port_end = randrange(port_start, 65536)
    port = str(port_start) + "-" + str(port_end)
  else: 
    port = str(port_start)

  ip_start = []
  for i in range(4):
    ip_start.append(randrange(256))
  if (random() < p_ip_range): 
    ip_end = [0] * 4
    higher = False
    for i in range(4):
      ip_end[i] = randrange(0 if higher else ip_start[i], 256)
      if (ip_end[i] > ip_start[i]):
        higher = True
    
    ip = ".".join([str(x) for x in ip_start]) + "-" + ".".join([str(x) for x in ip_end])
  else:
    ip = ".".join([str(x) for x in ip_start])


  entry.append(direction)
  entry.append(protocol)
  entry.append(port)
  entry.append(ip)

  entries.append(entry)

with open(output_file, "wb") as csv_file:
  writer = csv.writer(csv_file, delimiter=',')
  for entry in entries:
    writer.writerow(entry)

