package com.witsi.tools.shell;


/**
 * shell工具接口
 * @author hhr
 * @date 2016-11-25
 */
public interface Shellmoniter {

	public int excuteCmd(String paramString) throws Exception;
	
	public int excuteCmd(String[] paramString) throws Exception;

}
