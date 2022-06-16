package netty.handler;

import cloud.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CloudFileHandler extends SimpleChannelInboundHandler<CloudMessage>{

    private Path currentDir = Path.of("./UserFiles");
    private UsersData data;

    public CloudFileHandler(UsersData data) {
        this.data = data;
    }

    //    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        ctx.writeAndFlush(new ListFiles(currentDir));
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        if (cloudMessage instanceof UpdateListFiles update) {
            ctx.writeAndFlush(new ListFiles(currentDir));
        } else if (cloudMessage instanceof FileRequest fileRequest) {
            ctx.writeAndFlush(new FileMessage(currentDir.resolve(fileRequest.getName())));
        } else if (cloudMessage instanceof FileMessage fileMessage) {
            Files.write(currentDir.resolve(fileMessage.getName()), fileMessage.getData());
            ctx.writeAndFlush(new ListFiles(currentDir));
        } else if (cloudMessage instanceof PathRequest pathRequest) {
            currentDir = Path.of(pathRequest.getPath());
            ctx.writeAndFlush(new ListFiles(currentDir));
        } else if (cloudMessage instanceof Authentication auth) {
            if (data.getPassword(auth.getLogin()).equals(auth.getPassword())) {
                currentDir = currentDir.resolve(auth.getLogin());
                auth.setAuthStatus(true);
            } else {
                auth.setAuthStatus(false);
            }
            ctx.writeAndFlush(auth);
        } else if (cloudMessage instanceof Registration reg) {
            if (data.registration(reg.getLogin(), reg.getPassword())) {
                File folder = new File(String.valueOf(currentDir.resolve(reg.getLogin())));
                folder.mkdirs();
                reg.setRegStatus(true);
            } else {
                reg.setRegStatus(false);
            }
            ctx.writeAndFlush(reg);
        } else if (cloudMessage instanceof DeleteFile deleteFile) {
            File file = new File(deleteFile.getPath());
            deleteFile.setStatus(file.delete());
            ctx.writeAndFlush(deleteFile);
        } else if (cloudMessage instanceof CreateFolder createFolder) {
            File folder = new File(String.valueOf(Path.of(createFolder.getPath()).resolve(createFolder.getName())));
            folder.mkdir();
            ctx.writeAndFlush(new ListFiles(Path.of(createFolder.getPath())));
        }
    }
}
