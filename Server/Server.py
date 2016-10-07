import asyncore
import socket
import urllib2


class EchoHandler(asyncore.dispatcher):

    def handle_write(self):
        self.send("hello")
        self.close()


class EchoServer(asyncore.dispatcher):

    def __init__(self, host, port):
        asyncore.dispatcher.__init__(self)
        self.create_socket(socket.AF_INET, socket.SOCK_STREAM)
        self.set_reuse_addr()
        self.bind((host, port))
        self.listen(5)

    def handle_accept(self):
        pair = self.accept()
        if pair is not None:
            sock, addr = pair
            print 'Incoming connection from %s' % repr(addr)
            handler = EchoHandler(sock)


server = EchoServer('localhost', 8080)
asyncore.loop()
print "done"
