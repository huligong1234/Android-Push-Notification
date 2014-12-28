/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jeedevframework.jpush.server.console.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeedevframework.jpush.server.util.Config;
import org.jeedevframework.jpush.server.xmpp.push.NotificationManager;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/** 
 * A controller class to process the notification related requests.  
 *
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class NotificationController extends MultiActionController {

    private NotificationManager notificationManager;

    public NotificationController() {
        notificationManager = new NotificationManager();
    }

    public ModelAndView list(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        // mav.addObject("list", null);
        mav.setViewName("notification/form");
        return mav;
    }

    public ModelAndView send(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String broadcast = ServletRequestUtils.getStringParameter(request,"broadcast", "Y");
        String username = ServletRequestUtils.getStringParameter(request,"username");
        String title = ServletRequestUtils.getStringParameter(request, "title");
        String message = ServletRequestUtils.getStringParameter(request,"message");
        String uri = ServletRequestUtils.getStringParameter(request, "uri");
        String appId = ServletRequestUtils.getStringParameter(request, "appId");
        String showType = ServletRequestUtils.getStringParameter(request, "showType","NOTIFICATION");
        Integer timeToLive = ServletRequestUtils.getIntParameter(request, "timeToLive",0);
        
        String apiKey = Config.getString("apiKey", "");
        logger.debug("apiKey=" + apiKey);
        
        if (broadcast.equalsIgnoreCase("Y")) {//在线
            notificationManager.sendBroadcast(apiKey,showType, title, message, uri,timeToLive);
        }else if (broadcast.equalsIgnoreCase("A")) {//所有
            notificationManager.sendAllBroadcast(apiKey,showType, title, message, uri,timeToLive);
        }else {//个人
            notificationManager.sendNotifications(apiKey,appId,showType,username, title,message, uri,timeToLive);
        }

        ModelAndView mav = new ModelAndView();
        mav.setViewName("redirect:notification.do");
        return mav;
    }

}
