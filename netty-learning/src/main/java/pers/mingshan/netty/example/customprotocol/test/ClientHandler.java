package pers.mingshan.netty.example.customprotocol.test;

import java.io.UnsupportedEncodingException;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import pers.mingshan.netty.example.customprotocol.CustomProtocol;

public class ClientHandler extends ChannelInboundHandlerAdapter{
    private byte[] req;

    public ClientHandler() {
        req = ("Unless required by applicable dfslaw or agreed to in writing, software" +
                "  distributed under the License is distributed on an \"AS IS\" BASIS," +
                "  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied." +
                "  See the License for the specific language governing permissions and" +
                "  limitations under the License.This connector uses the BIO implementation that requires the JSSE" +
                "  style configuration. When using the APR/native implementation, the" +
                "  penSSL style configuration is required as described in the APR/native" +
                "  documentation.An Engine represents the entry point (within Catalina) that processes" +
                "  every request.  The Engine implementation for Tomcat stand alone" +
                "  analyzes the HTTP headers included with the request, and passes them" +
                "  on to the appropriate Host (virtual host)# Unless required by applicable law or agreed to in writing, software" +
                "# distributed under the License is distributed on an \"AS IS\" BASIS," +
                "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied." +
                "# See the License for the specific language governing permissions and" +
                "# limitations under the License.# For example, set the org.apache.catalina.util.LifecycleBase logger to log" +
                "# each component that extends LifecycleBase changing state:" +
                "#org.apache.catalina.util.LifecycleBase.level = FINE\n"
                ).getBytes();
    }

    @Override  
    public void channelActive(ChannelHandlerContext ctx) {   
        // 要发送的信息  
        //String data = "I am client ...";
        // 获得要发送信息的字节数组  
        byte[] content = req;
        // 要发送信息的长度  
        int contentLength = content.length;
  
        CustomProtocol protocol = new CustomProtocol(contentLength, content);
  
        ctx.writeAndFlush(protocol);
    }  

    @Override  
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException {  
        try {  
            // 用于获取客户端发来的数据信息  
            CustomProtocol body = (CustomProtocol) msg;  
            System.out.println("Client接受的客户端的信息 :" + body.toString() 
                + "\n-- 具体信息 : " + new String(body.getContent(), "UTF-8"));  
  
        } finally {  
            ReferenceCountUtil.release(msg);  
        }  
    }  

   @Override  
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {  
       if (this == ctx.pipeline().last()) {
           System.out.println("服务器已经关闭");
       }
       ctx.close();  
    }  
}
