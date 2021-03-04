
from rpcserver import server
import serv


server = server.ProtobufSocketServer(8888)
server.register_service(serv.GameMessage())
print("serving...")
server.run()
