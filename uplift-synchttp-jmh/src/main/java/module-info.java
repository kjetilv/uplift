/// @jenesis.alias jmh.core org.openjdk.jmh/jmh-core
/// @jenesis.bom pin-jmh.properties
/// @jenesis.bom pin-jackson.properties
/// @jenesis.release 25
/// @jenesis.pin io.netty.buffer 4.2.19.Final SHA-256/863f09b46cfe7d83872c9499086b9681cd9878c30a319d9f1f3a681bbfb0b12f
/// @jenesis.pin io.netty.codec 4.2.19.Final SHA-256/5702c8097cc81e939dd12e5b39ac2e81f83737c320f6f4d88553577842f9ceb6
/// @jenesis.pin io.netty.codec.http 4.2.19.Final SHA-256/7c28809a4f527746fbce2369f5ac5d5fcef45cfe9da21aa3dd9d3f6bdf00cbe0
/// @jenesis.pin io.netty.common 4.2.19.Final SHA-256/22fa74761f6e35c8c88972f35cf65664566b922dd73d3424528e97c34278fa79
/// @jenesis.pin io.netty.transport 4.2.19.Final SHA-256/d82d4e200456b8c5d8a1647a3724603f80616e5b35b9ada6e7a20d048f175075
/// @jenesis.pin io.netty/netty-buffer 4.2.19.Final SHA-256/863f09b46cfe7d83872c9499086b9681cd9878c30a319d9f1f3a681bbfb0b12f
/// @jenesis.pin io.netty/netty-codec-base 4.2.19.Final SHA-256/5702c8097cc81e939dd12e5b39ac2e81f83737c320f6f4d88553577842f9ceb6
/// @jenesis.pin io.netty/netty-codec-compression 4.2.19.Final SHA-256/8935c51c7217780ae56d178c938b50ea5729b1e088cf3c2568674e331665554c
/// @jenesis.pin io.netty/netty-codec-http 4.2.19.Final SHA-256/7c28809a4f527746fbce2369f5ac5d5fcef45cfe9da21aa3dd9d3f6bdf00cbe0
/// @jenesis.pin io.netty/netty-common 4.2.19.Final SHA-256/22fa74761f6e35c8c88972f35cf65664566b922dd73d3424528e97c34278fa79
/// @jenesis.pin io.netty/netty-handler 4.2.19.Final SHA-256/8b469cf23ddf21ba84291f4dff470d2277a28deac95e7084b0ff2ce853e58276
/// @jenesis.pin io.netty/netty-resolver 4.2.19.Final SHA-256/3ded47536faeb3c4c544a84871d82f461511589afb2a892a180ca425f281363e
/// @jenesis.pin io.netty/netty-transport 4.2.19.Final SHA-256/d82d4e200456b8c5d8a1647a3724603f80616e5b35b9ada6e7a20d048f175075
/// @jenesis.pin io.netty/netty-transport-native-unix-common 4.2.19.Final SHA-256/341e91b8e69e6b7d92c353c845ffd91f34e921b7186254c176a64c7089b3e721
module uplift.synchttp.jmh {

    requires java.net.http;

    requires uplift.synchttp;

    requires jmh.core;
    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.codec.http;
    requires io.netty.common;
    requires io.netty.transport;
}
