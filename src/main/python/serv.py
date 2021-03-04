import hello_world_pb2
import game_message_pb2
import time


class HelloWorldImpl(hello_world_pb2.HelloWorldService):
    def HelloWorld(self, controller, request, done):
        print("In HelloWorld!")

        # Print the request
        print(request)

        # Extract name from the message received
        name = request.my_name

        # Create a reply
        response = hello_world_pb2.HelloResponse()
        response.hello_world = 'Hello %s' % name

        # Sleeping to show asynchronous behavior on client end.
        time.sleep(1)

        # We're done, call the run method of the done callback
        done.run(response)


class GameMessage(game_message_pb2.IEchoService):
    def echo(self, controller, request, done):
        print("执行服务")
        response = game_message_pb2.ResponseMessage()
        response.msg = 'test tset test'
        done.run(response)


