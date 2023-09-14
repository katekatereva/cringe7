package ru.itmo.network.SelectorsOfServer;

import ru.itmo.dao.DAO;
import ru.itmo.dataManager.DataManager;
import ru.itmo.methods.HandlerOfRequests;
import ru.itmo.models.LabWork;
import ru.itmo.orm.ORM;
import ru.itmo.request.CommandRequest;
import ru.itmo.response.CommandResponse;
import ru.itmo.utilsCommon.DeserializerOfCommandRequestAndResponse;
import ru.itmo.utilsCommon.SerializerOfCommandRequestAndResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerConnect {

    private int port;
    private int bufferSize;
    private DataManager<LabWork> dataManager;
    private DAO<LabWork> dao;
    private ExecutorService threadPool;
    private ReadWriteLock lock;
    private ORM orm;

    public ServerConnect(int port, int bufferSize, DataManager<LabWork> dataManager, DAO<LabWork> dao, ORM orm) {
        this.port = port;
        this.bufferSize = bufferSize;
        this.dataManager = dataManager;
        this.dao = dao;
        this.threadPool = Executors.newCachedThreadPool();
        this.lock = new ReentrantReadWriteLock();
        this.orm = orm;
    }

    public void work() {
        try {
            Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

            System.out.println("Сервер готов к работе");
            while (true) {
                selector.select();

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isAcceptable()) {
                        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                        SocketChannel clientChannel = serverChannel.accept();
                        clientChannel.configureBlocking(false);
                        clientChannel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isWritable()) {
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        CommandRequest commandRequest = (CommandRequest) key.attachment();
                        buffer.clear();
                        buffer.put(SerializerOfCommandRequestAndResponse.serializeObject(HandlerOfRequests.handleRequestType(commandRequest, dataManager, dao, orm)));
                        buffer.flip();

                        threadPool.execute(() -> {
                            lock.readLock().lock();
                            try {
                                clientChannel.write(buffer);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                lock.readLock().unlock();
                            }

                            try {
                                clientChannel.register(selector, SelectionKey.OP_READ);
                            } catch (ClosedChannelException e) {
                                System.out.println("Сервер не смог обработать запрос");
                            }
                        });
                    } else if (key.isReadable()) {
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        buffer.clear();
                        int bytesRead = clientChannel.read(buffer);
                        if (bytesRead == -1) {
                            clientChannel.close();
                            continue;
                        }
                        CommandRequest commandRequest = DeserializerOfCommandRequestAndResponse.deserializeCommandRequest(buffer.array());

                        threadPool.execute(() -> {
                            lock.writeLock().lock();
                            try {
                                CommandResponse commandResponse = HandlerOfRequests.handleRequestType(commandRequest, dataManager, dao, orm);
                                clientChannel.register(selector, SelectionKey.OP_WRITE, commandResponse);
                            } catch (ClosedChannelException e) {
                                System.out.println("Сервер не смог обработать запрос");
                            } finally {
                                lock.writeLock().unlock();
                            }
                        });
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        threadPool.shutdown();
    }
}
