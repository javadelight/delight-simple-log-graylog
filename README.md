# delight-simple-log-graylog

A bridge from [delight-simple-log](https://github.com/javadelight/delight-simple-log) to [Graylog](https://www.graylog.org/).

# Usage

Simply create a GelfBridge and provide the details of your Graylog server.


```
Log.setListener(new GelfBridge("udp:myserver.com", 50000));

Log.info("Hello from Java!");
```
