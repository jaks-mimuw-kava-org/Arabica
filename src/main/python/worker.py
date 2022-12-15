import socket
from time import sleep


SLEEP_FOR = 0
CRLF = '\r\n'


class HttpRawRequest:
    def __init__(self, method, path, headers, body):
        self.method = method
        self.path = path
        self.headers = headers
        self.body = body

    def __str__(self):
        return f'{self.method} {self.path} HTTP/1.1{CRLF}' + \
               CRLF.join([f'{k}: {v}' for k, v in self.headers.items()]) + \
               CRLF * 2 + \
               self.body


class Worker:
    def __init__(self, host, port):
        self.host = host
        self.port = port
        self._socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    def connect(self):
        self._socket.connect((self.host, self.port))

    def send(self, message):
        for byte in message:
            sleep(SLEEP_FOR)
            print("Sending: '{}'".format(byte))
            self._socket.send(byte.encode())

    def receive(self):
        while True:
            data = self._socket.recv(1024)
            if not data:
                break
            print("Received: '{}'".format(data.decode()))


if __name__ == '__main__':
    worker = Worker('localhost', 4040)
    worker.connect()
    request = HttpRawRequest('GET', '/', {'Host': 'localhost', 'Content-Length': '5'}, 'Hello')
    print(request)
    worker.send(str(request))
    worker.receive()

