package pers.mingshan.netty.example.packetsplicing;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HelloWorldClientHandler extends ChannelInboundHandlerAdapter{
    private byte[] req;
    private int counter;

    public HelloWorldClientHandler() {
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
        ByteBuf message;

        //将上面的所有字符串作为一个消息体发送出去
        message = Unpooled.buffer(req.length);
        message.writeBytes(req);
        ctx.writeAndFlush(message);
    }  

    @Override  
    public void channelRead(ChannelHandlerContext ctx, Object msg) {  
        String buf = (String)msg;
        System.out.println("Now is : " + buf + " ; the counter is : "+ (++counter));
    }  

   @Override  
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {  
       cause.printStackTrace();  
       ctx.close();  
    }  
}
