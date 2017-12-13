
package com.witsi.tools;

import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

@SuppressLint("DefaultLocale")
public class TOOL 
{ 
//	private String TAG = "TOOL";
//	private boolean D = true;
	public static boolean ByteComp(byte[] x,int xPos, byte[] y,int yPos, int len)
    {
        if (x.length - xPos >= len && y.length - yPos >= len)
        {
            for (int i = 0; i < len; i++)
            {
                if (x[xPos + i] != y[yPos + i])
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

	public static byte[] subBytes(byte[] src, int begin, int count) 
	{         
		byte[] bs = new byte[count];         
		System.arraycopy(src, begin, bs, 0, count);
		return bs;     
	} 
	
	public static  long power(int x,int n)
	{
		int i =0;
		int tmp = 1;
		for(i = 0;i<n;i++){
			tmp = tmp*x;
		}
		return tmp;
	}
	
	public static long  BCDtoDec(byte bcd[],int offset, int length)
	{
	     int i, tmp;
	     long dec = 0;
	     for(i = 0 + offset; i < length + offset; i++)
	     {
	        tmp = (( bcd[i] >> 4 ) & 0x0F ) * 10 + ( bcd[i] & 0x0F );   
	        
	        
	        dec += tmp * power(100, length + offset - 1 - i);          
	     }
	     return dec;
	}
	
	public static int DectoBCD(int Dec, byte[] Bcd, int length)
	{
	     int i;
	     int temp;
	     for(i = length-1; i >= 0; i--)
	     {
	         temp = Dec%100;
	         Bcd[i] = (byte) (((temp/10)<<4) + ((temp%10) & 0x0F));
	         Dec /= 100;
	     }
	     return 0;
	}
	
	public static int DectoBCD(long Dec, byte[] Bcd, int length)
	{
	     int i;
	     long temp;
	     for(i = length-1; i >= 0; i--)
	     {
	         temp = Dec%100;
	         Bcd[i] = (byte) (((temp/10)<<4) + ((temp%10) & 0x0F));
	         Dec /= 100;
	     }
	     return 0;
	}
	
	public static int DectoBCD(long Dec, byte[] Bcd,int offset, int length)
	{
	     int i;
	     long temp;
	     for(i = length-1; i >= 0; i--)
	     {
	         temp = Dec%100;
	         Bcd[i + offset] = (byte) (((temp/10)<<4) + ((temp%10) & 0x0F));
	         Dec /= 100;
	     }
	     return 0;
	}
	
	//十六进制字符串转换成十六进制byte数组
	public static byte[] hexStr2HexByte(String hexStr) 
    {   
        String str = "0123456789ABCDEF"; 
        char[] hexs = hexStr.toUpperCase().toCharArray();   
        byte[] bytes = new byte[hexStr.length() / 2];   
        int n;   
 
        for (int i = 0; i < bytes.length; i++) 
        {   

            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);   
        }   

        return bytes;   
    } 
	
	//十六进制byte数组转换成十六进制字符串
    public static String hexByte2HexStr(byte[] bs) 
    {   
    	if(bs == null || bs.length == 0)
    		return null;
    	
        char[] chars = "0123456789ABCDEF".toCharArray();   
        StringBuilder sb = new StringBuilder(""); 
        int bit;   
         
        for (int i = 0; i < bs.length; i++) 
        {   
            bit = (bs[i] & 0x0f0) >> 4;   
            sb.append(chars[bit]);   
            bit = bs[i] & 0x0f;   
            sb.append(chars[bit]); 
            sb.append(' '); 
        }   
        return sb.toString().trim();   
    } 
    
	//十六进制byte数组转换成十六进制字符串
    public static String logHexByte2HexStr(byte[] bs) 
    {   
    	if(bs == null || bs.length == 0)
    		return null;
    	
        char[] chars = "0123456789ABCDEF".toCharArray();   
        StringBuilder sb = new StringBuilder(""); 
        int bit;   
         
        for (int i = 0; i < bs.length; i++) 
        {   
            bit = (bs[i] & 0x0f0) >> 4;   
            sb.append(chars[bit]);   
            bit = bs[i] & 0x0f;   
            sb.append(chars[bit]); 
//            sb.append(' '); 
        }   
        return sb.toString().trim();   
    } 
    
	//十六进制byte数组转换成十六进制字符串
    public static String hexByte2HexStr(byte[] bs, int offst, int len) 
    {   
 
        char[] chars = "0123456789ABCDEF".toCharArray();   
        StringBuilder sb = new StringBuilder(""); 
        int bit;   
         
        for (int i = offst; i < offst + len; i++) 
        {   
            bit = (bs[i] & 0x0f0) >> 4;   
            sb.append(chars[bit]);   
            bit = bs[i] & 0x0f;   
            sb.append(chars[bit]); 
//            sb.append(' '); 
        }   
        return sb.toString().trim();   
    } 
    
	//十六进制byte数组转换成十六进制字符串
    public static String logHexByte2HexStr(byte[] bs, int offst, int len) 
    {   
 
        char[] chars = "0123456789ABCDEF".toCharArray();   
        StringBuilder sb = new StringBuilder(""); 
        int bit;   
         
        for (int i = offst; i < offst + len; i++) 
        {   
            bit = (bs[i] & 0x0f0) >> 4;   
            sb.append(chars[bit]);   
            bit = bs[i] & 0x0f;   
            sb.append(chars[bit]); 
            sb.append(' '); 
        }   
        return sb.toString().trim();   
    } 
    
    
    public static int asc2bcd(byte[] ascStr, int ascOffset, int asclen, byte[] bcdStr, 
    		int bcdOffset, int align)
    {
        int i = 0;
        byte ch;

        for (i = 0; i < (asclen + 1) / 2; i++)
            bcdStr[bcdOffset + i] = 0x00;

        if ((asclen & 0x01) > 0 && align > 0)
            align = 1;
        else
            align = 0;

        for (i = align; i < asclen + align; i ++) {
            ch = ascStr[ascOffset + i - align];
            if (ch >= 'a'&& ch <= 'f')
                ch -= ('a' - 0x0A);
            else if (ch >= 'A'&& ch <= 'F')
                ch -= ('A' - 0x0A);
            else if (ch >= '0' && ch <= '9')
                ch -= '0';
            else
                return -1;
            bcdStr[bcdOffset + (i / 2)] |= (ch << (((i + 1) % 2) * 4));
        }

        return 0;
    }
    
/*    功能:        转换BCD码为ASCII码。如果有10->15数值，将转
　　　　换为大写字母: A->F。
 *    参数:        #bcd        输入的BCD码字符
                 #asclen        输出的ASCII码字符串长度
                 #asc        输出的ASCII码字符串
                 #align        1        BCD左填充0x00
                             0        BCD右填充0x00
 *    返回值:        0        成功
 *                -1        失败
 */
    public static int bcd2asc(byte[] bcd,int bcdOffset, int asclen, byte[] asc, int align)
    {
        int cnt;
        int pbcd = bcdOffset;
        int pasc = 0;

        if (((asclen & 0x01)> 0) && (align > 0)) {  /*判别是否为奇数以及往那边对齐 */
            cnt = 1;
            asclen ++;
        } else {
            cnt = 0;
        }
        for (; cnt < asclen; cnt++, pasc++) {
        	
        	asc[pasc] = (byte) ((cnt & 0x01)>0 ? (bcd[pbcd ++] & 0x0f) 
        			: ((bcd[pbcd]&0xff) >> 4));
        	asc[pasc] += ((asc[pasc] > 9) ? ('A' - 10) : '0');
        }
        asc[pasc] = 0;
        return 0;
    }

	 public static int bytes2int(byte[] bytes) {
	        int val = 0;
	        for (int i = 3; i >= 0; i--) {
	            val += ((int)bytes[i]&0xFF)<<(8*i);
	        }
	        return val;
	 }
    
	 
    public static void ProgramSleep(long sleepTime)
	{
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    public static String getDevId()
  	{
    	String serialnum = null;  
    	try {   
    	 Class<?> c = Class.forName("android.os.SystemProperties"); 
    	 Method get = c.getMethod("get", String.class, String.class );
    	 serialnum = (String)(   get.invoke(c, "ro.serialno", "unknown" )  );
    	}                                                                                
    	catch (Exception ignored)
    	{                      
    	}
    	return serialnum;
  	}
    
	public static String toFormtAmt(long val)
	{
	      return String.format("%d.%02d", val/100 , val%100);
	} 
	
    public static boolean isPad(Context context) {
   	 WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
   	 Display display = wm.getDefaultDisplay();
   	 // 屏幕宽度
   	 float screenWidth = display.getWidth();
   	 // 屏幕高度
   	 float screenHeight = display.getHeight();
   	 DisplayMetrics dm = new DisplayMetrics();
   	 display.getMetrics(dm);
   	 double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
   	 double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
   	 // 屏幕尺寸
   	 double screenInches = Math.sqrt(x + y);
   	 Log.v("TOOL", "width is: " + screenWidth + " height is: " + screenHeight
   			 + " x is: " + x + " y is: " + y 
   			 + " inches is: " + screenInches);
   	 // 大于6尺寸则为Pad
   	 if (screenInches >= 6.0) {
   	  return true;
   	 }
   	 return false;
   	}
}
