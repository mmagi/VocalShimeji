//
//  vocalshimejibridge.h
//  vocalshimejibridge
//
//  Created by magi on 27/11/13.
//  Copyright (c) 2013年 magi. All rights reserved.
//
#import <Foundation/Foundation.h>
#import <ScriptingBridge/ScriptingBridge.h>
#import "Safari.h"
int initShimejiBridge();
int resetSafariPos();
int getDesktopArea(int bounds []);
int getSafariArea(int bounds []);
int moveSafariTo(const int x,const int y,int bounds[]);
