package com.gompang.handler;

import com.gompang.dispatcher.PacketDispatcher;
import com.gompang.packet.Packet;
import com.gompang.manager.ServerManager;
import com.gompang.manager.StatisticsManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/*
    Created By Gompangs(stacks5978) at 2018. 4. 11.
    blog : http://gompangs.tistory.com/
*/

@ChannelHandler.Sharable
@Component
public class BaseInboundHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private StatisticsManager statisticsManager;

    @Autowired
    private ServerManager serverManager;

    @Autowired
    private PacketDispatcher packetDispatcher;


    @PostConstruct
    public void init() {
        logger.info("BaseHandler init");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("channel activated {}", ctx.channel());
        serverManager.getChannels().add(ctx.channel());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("channel deactivated {}", ctx.channel());
        serverManager.getChannels().remove(ctx.channel());
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Packet) {
            Packet packet = (Packet) msg;
            statisticsManager.read(msg);

            byte[] response = packetDispatcher.dispatch(ctx, packet);
            ctx.writeAndFlush(response);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("{} raised exception ==> {}", ctx.channel(), cause.getMessage());
        cause.printStackTrace();
    }
}
