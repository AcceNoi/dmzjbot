package org.accen.dmzj.core.handler.cmd;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.accen.dmzj.core.handler.listen.GroupRepeatListener;
import org.accen.dmzj.core.task.GeneralTask;
import org.accen.dmzj.core.task.api.SaucenaoApiClientPk;
import org.accen.dmzj.core.task.api.vo.ImageResult;
import org.accen.dmzj.util.CQUtil;
import org.accen.dmzj.util.SystemUtil;
import org.accen.dmzj.web.vo.Qmessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ImageSearchCmd implements CmdAdapter {

	@Value("${coolq.base.home}")
	private String coolqHome;
	@Autowired
	private SaucenaoApiClientPk saucenaoApiClient;
	
	private final static Logger logger = LoggerFactory.getLogger(GroupRepeatListener.class);
	@Value("${coolq.imagesearch.fav.increase:1}")
	private int increase = 1;
	
	@Autowired
	private CheckinCmd checkinCmd;
	
	@Override
	public String describe() {
		return "检索网络图片";
	}

	@Override
	public String example() {
		return "龙妈找图[图片]";
	}
	
	private final static Pattern patternCmd = Pattern
			.compile("^老婆找图(\\[CQ\\:image,file=.*?\\])?$");
	private final static Pattern patternCq = Pattern
			.compile("^\\[CQ\\:image,file=(.*?),url=(.*?)\\]$");

	@Override
	public GeneralTask cmdAdapt(Qmessage qmessage, String selfQnum) {
		// TODO Auto-generated method stub
		//http://saucenao.com/
		
		String message = qmessage.getMessage().trim();
		Matcher matcher = patternCmd.matcher(message);
		if(matcher.matches()) {
			GeneralTask task =  new GeneralTask();
		
			task.setSelfQnum(selfQnum);
			task.setType(qmessage.getMessageType());
			task.setTargetId(qmessage.getGroupId());
			String imageCq = matcher.group(1);
			if(imageCq==null) {
				//TODO 手机qq无法同时发消息和图片，这里要回复“请发送图片”，并监听该id的下次消息
			}else {
				matcher = patternCq.matcher(imageCq);
				if(matcher.find()) {
					/*String imageFile = coolqHome
							+SystemUtil.getFileSeperate()
							+"data"
							+SystemUtil.getFileSeperate()
							+"image"
							+SystemUtil.getFileSeperate()
							+matcher.group(1);
					ImageResult imageResult = saucenaoApiClient.search(new File(imageFile));*/
					ImageResult imageResult = saucenaoApiClient.search(matcher.group(2));
					if(imageResult==null||!imageResult.isSuccess()) {
						task.setMessage(CQUtil.at(qmessage.getUserId())+"图片检索不到喵...");
					}else {
						
						//增加好感度
						int curFav = checkinCmd.modifyFav(qmessage.getMessageType(), qmessage.getGroupId(), qmessage.getUserId(), increase);
						
						task.setMessage(CQUtil.at(qmessage.getUserId())
								+"检索到图片喵！相似度："
								+imageResult.getSimilarity()
								+"。标题："
								+imageResult.getTitle()
								+"。来源："
								+imageResult.getContent()
								+(curFav<0?"（绑定可以增加好感度哦喵~）":("增加"+increase+"点好感喵~，当前好感度:"+curFav)));
					}
					return task;
				}else {
					logger.warn("消息：${0}获取图片失败！", qmessage.getId());
				}
			}
		}
		return null;
	}

}
