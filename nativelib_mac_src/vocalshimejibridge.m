//
//  vocalshimejibridge.m
//  vocalshimejibridge
//
//  Created by magi on 27/11/13.
//  Copyright (c) 2013年 magi. All rights reserved.
//

#import "vocalshimejibridge.h"



SafariApplication *safari;
int initShimejiBridge(){@autoreleasepool{
    safari = [SBApplication applicationWithBundleIdentifier:@"com.apple.Safari"];
    return 0;
}}

int resetSafariPos() {@autoreleasepool{
    int ret = 0;
    if ([safari isRunning]){
        NSRect vframe = [[NSScreen mainScreen] visibleFrame];
        NSRect fframe = [[NSScreen mainScreen] frame];
        vframe.origin.y=fframe.origin.y+fframe.size.height-vframe.origin.y-vframe.size.height;
        for(SafariWindow * win in [safari windows]){
            if (![win visible] && ![win miniaturized]){
                ret = 1;
                win.visible = YES;
            }
            NSRect bounds = [win bounds];
            if (!NSContainsRect(vframe,bounds)){
                if(bounds.size.width<=vframe.size.width && bounds.size.height<=vframe.size.height){//center it
                    bounds.origin.x = vframe.origin.x + (vframe.size.width-bounds.size.width)/2;
                    bounds.origin.y = vframe.origin.y + (vframe.size.height-bounds.size.height)/2 ;
                    win.bounds = bounds;
                }else{
                    win.bounds = vframe;
                }
            }
        }
    }
    return ret;
}}
int getDesktopArea(int area []){@autoreleasepool{
    NSRect vframe = [[NSScreen mainScreen] visibleFrame];
    NSRect fframe = [[NSScreen mainScreen] frame];
    area[0]=vframe.origin.x;
    area[1]=fframe.origin.y+fframe.size.height-vframe.origin.y-vframe.size.height;
    area[2]=vframe.size.width;
    area[3]=vframe.size.height;
    return 1;
}}
int getSafariArea(int area []){@autoreleasepool{
    if ([safari isRunning]){
        SafariWindow * win = [[safari windows] firstObject];
        if([win visible]){
            NSRect bounds = [win bounds];
            area[0]=bounds.origin.x;
            area[1]=bounds.origin.y;
            area[2]=bounds.size.width;
            area[3]=bounds.size.height;
            return 1;
        }
    }
    return 0;
}}
int moveSafariTo(const int x,const int y,int area []){@autoreleasepool{
    if ([safari isRunning]){
        NSRect vframe = [[NSScreen mainScreen] visibleFrame];
        NSRect fframe = [[NSScreen mainScreen] frame];
        vframe.origin.y=fframe.origin.y+fframe.size.height-vframe.origin.y-vframe.size.height;
        SafariWindow * win = [[safari windows] firstObject];
        if([win visible]){
            NSRect bounds = [win bounds];
            bounds.origin.x=x;
            bounds.origin.y=y;
            if (!NSIntersectsRect(vframe,bounds)){
                win.visible = NO;
                return 0;
            }
            win.bounds = bounds;
            area[0]=bounds.origin.x;
            area[1]=bounds.origin.y;
            area[2]=bounds.size.width;
            area[3]=bounds.size.height;
            return 1;
        }
    }
    return 0;
}}
