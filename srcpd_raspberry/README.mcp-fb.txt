This enhancement of srcpd allows using the spi-16bit port Expander "MCP23S17" for getting feedback from srcpd as "FB......". 
It is available on RaspberryPi only.
Connect up to 8 MCP23S17 chips according to this website
http://erik-bartmann.de/raspberry-pi-2-auflage-downloads/
get the document PiMeUp_MCP23S17 and connect the Chip as described on page 6.
I simplified this by omitting following items:
R13 and LED9 (I do not necessarily need power indication),
R5 to R12 (I configured the MCP23s17 for using its internal pullup resistors)
s2: I do not need the reset button. The srcpd reintializes the chips roughly 
every 2 mintues,
so even if you disconnect it from power supply 
(I spent an extra 3,3V where GND is connected to RaspberryPi GND).

My example: On Bus 2 FB No 31 is activated. This is done by connecting the adjacent pins GPA0..7 or GPB0..7 to GND; srcpd will deliver something like this:
1522458431.476 100 INFO 2 FB 31 1
Be carefully not to connect any higher voltage. For reading the occupation state of a track section I use optocouplers that connect GND to the GPA0..7 or GPB0..7 pins. (SFH615 for example).

There are some configuration possibilities that 
I am going to explain according to my config-file:

<?xml version="1.0"?>
<srcpd version="2.0">
  <bus number="0">
    <server>
      <tcp-port>4303</tcp-port>
      <pid-file>/var/run/srcpd.pid</pid-file>
      <username>srcpd</username>
      <groupname>srcpd</groupname>
    </server>
    <verbosity>0</verbosity>
  </bus>
  <bus number="1">
    <ddl>
      <number_ga>300</number_ga>
      <number_gl>150</number_gl>
      <enable_maerklin>yes</enable_maerklin>
      <enable_nmradcc>yes</enable_nmradcc>
      <enable_usleep_patch>yes</enable_usleep_patch>
    </ddl>
    <use_watchdog>no</use_watchdog>
    <verbosity>0</verbosity>
    <auto_power_on>yes</auto_power_on>
    <device>/dev/spidev0.0</device>
   </bus>

//here we are starting the mcp feedback on the Raspberrypi's //srcpd as bus 2:

   <bus number="2">
     <mcp>

// you can connect up to 8 MCP23S17 chips. I connected three // of them with adresses 0, 1, 2, so number_fb_1 is set to 3, // range is [1..8]  
     <number_fb_1>3</number_fb_1>

// I tested two values, 0 and 50 us. 0 does not work, 50 is 
// default and sufficient for me, the chips are connected to // the raspberrypi via Cat5 patchcables of 20cm, 5m and 3m. 
// If you have longer wires, enhancing this values may have a // positive effect.
       <waitus>50</waitus>
// if you like to connect to other Raspberry pins configure 
// this here. Given are the pin numbers, I don't take care 
// about different numbering of Raspis versions.
// These values are default and can be omitted in case it 
// meets your setup. See also page 13 in the above cited
// document of Mr. Bartmann
       <miso>23</miso>
       <mosi>24</mosi>
       <sclk>18</sclk>
       <cs_1>25</cs_1>
     </mcp>
   </bus>
</srcpd>


 Currently I realized one bus only, so the number of FB ports is limited 
to 8*16 = 128.
For example if one more GPIO pin is available 
we could use this for another FB-Bus if I find motivatin for doing so, 
this is currently not impemented.
There exists an implementation of a python proxy, 
adding the FB to the srcp on port 4304. 
I used this with the original version of srcpd 2.1.3, 
but was not satisfied because of unreliable behaviour of the FB messages.
So I integrated operation of the MCP23S17 directly into srcpd. 
This results in much more reliable section state (FB) and this even 
in case you connect further srcp-clients in a biger layout. 
Furthermore I based on  the improved Version of Daniel Sigg 
which properly operates all of my Motorola devices 
via SPI-interface (see bus 1 in above configuration and hints on 
http://siggsoftware.ch/wordpress/  ).

ruediger.seidel at web.de
 