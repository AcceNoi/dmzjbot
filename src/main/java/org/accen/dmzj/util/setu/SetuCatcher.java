package org.accen.dmzj.util.setu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.accen.dmzj.util.FilePersistentUtil;
import org.accen.dmzj.util.RandomUtil;
import org.accen.dmzj.web.vo.Qmessage;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class SetuCatcher {
	private static final String SETU_DIR = "setu/";
	public static final String SETU_SUFFIX = ".accen";
	private long STD_TIME_COUNTER = 0l;
	/**
	 * 保存当前处理的图片md5，避免重复抓取
	 */
	private static final Set<String> avoidRepeatingMd5 = new HashSet<String>(64);
	private static final Set<String> avoidRepeatingPid = new HashSet<String>(64);
	public SetuCatcher() {
		File home = new File(SETU_DIR);
		if(!home.exists()) {
			home.mkdir();
		}
		try {
			STD_TIME_COUNTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-01-01 00:00:00").getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	@Autowired
	private FilePersistentUtil filePersistentUtil;
	/**
	 * 从qmessage中抓取涩图
	 * @param cqImage
	 * @param fileName 仅文件名，非全路径文件名
	 */
	public boolean catchFromCqImage(String cqImage,String fileName) {
		String[] infos = filePersistentUtil.getImageMetaInfo(cqImage);
		if(avoidRepeatingMd5.contains(infos[0].trim())) {
			return false;
		}else {
			filePersistentUtil.persistentByCq(cqImage, fileName, SETU_DIR);
			avoidRepeatingMd5.add(infos[0].trim());
			return true;
		}
	}
	/**
	 * 从pid中抓取涩图
	 * @param pid
	 * @param fileName
	 * @return
	 */
	public boolean catchFromPid(String pid,String fileName) {
		if(avoidRepeatingPid.contains(pid)) {
			return false;
		}else {
			String url = "https://pixiv.cat/"+pid+".jpg";
			filePersistentUtil.persistent(url, fileName, SETU_DIR);
			avoidRepeatingPid.add(pid);
			return true;
		}
		
	}
	/**
	 * 从is中抓取涩图
	 * @param pid
	 * @param is
	 * @return
	 */
	@Deprecated
	public boolean catchFromInputStream(String pid,InputStream is) {
		if(avoidRepeatingPid.contains(pid)||is==null) {
			return false;
		}else {
			File setu = new File(SETU_DIR+"/"+pid+".accen");
			try(OutputStream os = new FileOutputStream(setu);){
				IOUtils.copy(is, os);
				os.flush();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
	}
	/**
	 * 随机获得一个涩图
	 * @return
	 */
	public File randomSetu() {
		String[] childs = setuList();
		if(childs.length<=0) {
			return null;
		}
		return new File(SETU_DIR+childs[RandomUtil.randomInt(childs.length)]);
		
	}
	/**
	 * 随机获得count张不重复的涩图
	 * @param count
	 * @return
	 */
	public File[] randomSetu(int count) {
		String[] childs = setuList();
		if(childs.length<=0) {
			return null;
		}
		return Arrays.stream(RandomUtil.randomInt(childs.length, count))
			.boxed()
			.map(index->new File(SETU_DIR+childs[index]))
			.toArray(File[]::new);
	}
	
	private String[] setuList() {
		File home = new File(SETU_DIR);
		String[] childs = home.list(( dir,  name) ->{return name.endsWith(SETU_SUFFIX);});
		return childs;
	
	}
	
	/**
	 * 随机生成涩图名
	 * @param qmessage
	 * @return
	 */
	public String uuSetuName(Qmessage qmessage) {
		long curTime = new Date().getTime();
		String createNickName = (String) ((Map<String, Object>)qmessage.getEvent().get("sender")).get("nickname");
		return createNickName+"@"+(curTime-STD_TIME_COUNTER)+SETU_SUFFIX;
	}
}
