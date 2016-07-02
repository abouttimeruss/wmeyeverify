//
//  IrisAccess.m
//  SilentShot
//
//  Created by Nero Wolfe on 12/01/16.
//
//

#import "IrisAccess.h"
//#import <Cordova/NSData+Base64.h>

#import "ScanningOverlayView.h"
#define CDV_PHOTO_PREFIX @"irisaccess_photo_"

@implementation IrisAccess 
{
    EyeVerifyLoader *evLoader;
    BOOL isCameraReady;
    NSInteger scanType;
    NSString *userNameFromOptions;
    NSString *userKeyFromOptions;
    UILabel *message;
    UILabel *counter;
    UIProgressView *progress;
    ScanningOverlayView *scanOverlay;
}

-(void)getIris:(CDVInvokedUrlCommand *)command
{
    self.hasPendingOperation = YES;
    self.latestCommand = command;
    [self parseCommandArguments:command.arguments];
    [self setDefaults];
    
    [self performSelectorInBackground:@selector(startIris) withObject:nil];

    
    //[self startIris];
    /*if(isCameraReady)
    {
        
        [self performSelectorInBackground:@selector(processingCamera) withObject:nil];
        
    }
    else
    {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Camera is not ready"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        self.hasPendingOperation = NO;
    }*/
    
    
    
}


-(void)processingCamera
{
    

    
    


   



}


-(void)startIris {
    if(scanType == 0)
    {
        [self enroll];

    }
    else
    {
        [self verify];
    }
}


- (void) enroll {
    
    __block CDVPluginResult* enrolResult = nil;
    
    EyeVerify *ev = [EyeVerifyLoader getEyeVerifyInstance];
    if (ev) {
        [ev setEVAuthSessionDelegate:self];
        
        [ev enrollUser:ev.userName userKey:[userKeyFromOptions dataUsingEncoding:NSUTF8StringEncoding] completion:^(EVEnrollmentResult result, NSData *userKey, EVAbortReason abort_reason) {
            
            switch (result) {
                case EVEnrollmentResultNoEyes:
                case EVEnrollmentResultNoop:
                case EVEnrollmentResultAborted:
                case EVEnrollmentResultBadMatch:
                case EVEnrollmentResultLowLight:
                case EVEnrollmentResultNothing:
                case EVEnrollmentResultBadQuality:
                case EVEnrollmentResultIncomplete:
                case EVEnrollmentResultHTTPError:
                case  EVEnrollmentResultZeroImages:
                case EVEnrollmentResultEyenessFailed:
                case EVEnrollmentResultError:
                {
                    enrolResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[NSString stringWithFormat:@"EVEnrollmentResult: %li, EVAbortReason: %li", result, (long)abort_reason]];
                    [[self.viewController.view viewWithTag:990] removeFromSuperview];
                    [message removeFromSuperview];
                    [progress removeFromSuperview];
                    [counter removeFromSuperview];
                    [scanOverlay removeFromSuperview];
                    if (enrolResult) {
                        [self.commandDelegate sendPluginResult:enrolResult callbackId:_latestCommand.callbackId];
                    }
                    self.hasPendingOperation = NO;
                }
                    break;
                    
                case EVEnrollmentResultSuccess:
                {
                    NSLog(@"Enrollment: enrolled=%d; userKey=%@ error=%@", YES, userKey != nil ? [[NSString alloc] initWithData:userKey encoding:NSUTF8StringEncoding] : @"nil", @"none");
                    enrolResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[[NSString alloc] initWithData:userKey encoding:NSUTF8StringEncoding]];
                    [[self.viewController.view viewWithTag:990] removeFromSuperview];
                    [message removeFromSuperview];
                    [progress removeFromSuperview];
                    [counter removeFromSuperview];
                    [scanOverlay removeFromSuperview];
                    if (enrolResult) {
                        [self.commandDelegate sendPluginResult:enrolResult callbackId:_latestCommand.callbackId];
                    }
                    self.hasPendingOperation = NO;
                }
                    
                    break;
                default:
                    break;
            }
        }];
        
        /*[ev enrollUser:ev.userName userKey:[userKeyFromOptions dataUsingEncoding:NSUTF8StringEncoding] localCompletionBlock:^(BOOL enrolled, NSData *userKey, NSError *error) {
            NSLog(@"Enrollment: enrolled=%d; userKey=%@ error=%@", enrolled, userKey != nil ? [[NSString alloc] initWithData:userKey encoding:NSUTF8StringEncoding] : @"nil", error);
            if(enrolled)
            {
                
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[[NSString alloc] initWithData:userKey encoding:NSUTF8StringEncoding]];

            }
            else
            {
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""];

            }
            [[self.viewController.view viewWithTag:990] removeFromSuperview];
            [message removeFromSuperview];
            [progress removeFromSuperview];
            [counter removeFromSuperview];
            [scanOverlay removeFromSuperview];
            if (result) {
                [self.commandDelegate sendPluginResult:result callbackId:_latestCommand.callbackId];
            }
            self.hasPendingOperation = NO;
           
        }];*/
    }
}

- (void) verify {

    __block CDVPluginResult* verifyResult = nil;

    EyeVerify *ev = [EyeVerifyLoader getEyeVerifyInstance];
    if (ev) {
        [ev setEVAuthSessionDelegate:self];
        
        [ev verifyUser:ev.userName completion:^(EVVerifyResult result, NSData *userKey, EVAbortReason abort_reason) {
           
            switch (result) {
                case EVVerifyResultNoop:
                case EVVerifyResultAborted:
                case EVVerifyResultNothing:
                case EVVerifyResultNotMatch:
                case EVVerifyResultHTTPError:
                case EVVerifyResultBadQuality:
                case EVVerifyResultZeroImages:
                case EVVerifyResultKeyGenFailed:
                case EVVerifyResultNoEnrollments:
                case EVVerifyResultLivenessFailed:
                case EVVerifyResultNoServerAuthData:
                case EVVerifyResultCannotComputeFeature:
                case EVVerifyResultError:
                {
                    verifyResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:@[@(NO),[NSString stringWithFormat:@"Not Verified. EVVerifyResult: %li, EVAbortReason: %li", result, abort_reason]]];
                    [[self.viewController.view viewWithTag:990] removeFromSuperview];
                    [message removeFromSuperview];
                    [progress removeFromSuperview];
                    [counter removeFromSuperview];
                    [scanOverlay removeFromSuperview];
                    if (verifyResult) {
                        [self.commandDelegate sendPluginResult:verifyResult callbackId:_latestCommand.callbackId];
                    }
                    self.hasPendingOperation = NO;

                }
                    break;
                case EVVerifyResultMatch:
                case EVVerifyResultMatchWithEnroll:
                {
                    NSLog(@"Verifying: verified=%d; userKey=%@ error=%@", YES, userKey != nil ? [[NSString alloc] initWithData:userKey encoding:NSUTF8StringEncoding] : @"nil", @"none");
                    verifyResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:@[@(YES), userKey != nil ? [[NSString alloc] initWithData:userKey encoding:NSUTF8StringEncoding] : @"nil"]];
                   
                    [[self.viewController.view viewWithTag:990] removeFromSuperview];
                    [message removeFromSuperview];
                    [progress removeFromSuperview];
                    [counter removeFromSuperview];
                    [scanOverlay removeFromSuperview];
                    if (verifyResult) {
                        [self.commandDelegate sendPluginResult:verifyResult callbackId:_latestCommand.callbackId];
                    }
                    self.hasPendingOperation = NO;
                }
                    break;
                    
                default:
                    break;
            }
        }];
        
       
    }
}

- (UIColor *)colorFromHexString:(NSString *)hexString {
    unsigned rgbValue = 0;
    NSScanner *scanner = [NSScanner scannerWithString:hexString];
    [scanner setScanLocation:1]; // bypass '#' character
    [scanner scanHexInt:&rgbValue];
    return [UIColor colorWithRed:((rgbValue & 0xFF0000) >> 16)/255.0 green:((rgbValue & 0xFF00) >> 8)/255.0 blue:(rgbValue & 0xFF)/255.0 alpha:1.0];
}

-(void)setDefaults {
    evLoader = [[EyeVerifyLoader alloc] init];
    [evLoader loadEyeVerifyWithLicense:@"1DBRJYSHENYXWOK0"];

    EyeVerify *ev = [EyeVerifyLoader getEyeVerifyInstance];
    ev.userName = userNameFromOptions;

    UIView *vv = [[UIView alloc] initWithFrame:CGRectMake(20, 100, self.viewController.view.frame.size.width - 40, 120)];
    [self.viewController.view addSubview:vv];
    vv.tag = 990;
    message = [[UILabel alloc] initWithFrame:CGRectMake(20, 230, self.viewController.view.frame.size.width - 40, 70)];
    message.textAlignment = NSTextAlignmentCenter;
    message.font = [UIFont systemFontOfSize:16];
    message.backgroundColor = [UIColor whiteColor];
    message.textColor = [UIColor darkTextColor];
    message.numberOfLines = 0;
    message.lineBreakMode = NSLineBreakByWordWrapping;
    [self.viewController.view addSubview:message];
    
    counter = [[UILabel alloc] initWithFrame:CGRectMake(self.viewController.view.frame.size.width / 2 - 17, 85, 34, 34)];
    counter.textAlignment = NSTextAlignmentCenter;
    counter.font = [UIFont boldSystemFontOfSize:30];
    counter.backgroundColor = [UIColor clearColor];
    counter.textColor = [self colorFromHexString:@"5C8A00"];
    counter.alpha = 0.9;
    
    progress = [[UIProgressView alloc] initWithFrame:CGRectMake(20, 220, self.viewController.view.frame.size.width - 40, 3)];
    progress.progress = 0.0;
    progress.progressTintColor = [self colorFromHexString:@"84B533"];
    progress.backgroundColor = [UIColor whiteColor];
    [self.viewController.view addSubview:progress];

    
    scanOverlay = [[ScanningOverlayView alloc] initWithFrame:vv.frame];
    scanOverlay.targetHighlighted = YES;
    scanOverlay.hidden = YES;
    scanOverlay.backgroundColor = [UIColor clearColor];
    [self.viewController.view addSubview:scanOverlay];
    [self.viewController.view addSubview:counter];

    [ev setCaptureView:vv];

    
}

-(void)parseCommandArguments:(NSArray*) args
{
    
    if(args.count > 0)
    {
        NSDictionary *arguments = args[0];

        if(arguments[@"scanType"])
        {
            NSInteger dest = [arguments[@"scanType"] integerValue];
            scanType = dest;
        }
        if(arguments[@"userName"])
        {
            userNameFromOptions = arguments[@"userName"];

        }
        if(arguments[@"userKey"])
        {
            userKeyFromOptions = arguments[@"userKey"];
            
        }
        NSLog(@"userName: %@   userKey: %@   scanType: %li", userNameFromOptions, userKeyFromOptions, scanType);

    }
    else
    {
        scanType = 1;
        userNameFromOptions = @"sample";
        userKeyFromOptions = @"1234fhshfsf678906867";
    }
    
}


- (void) eyeStatusChanged:(EVEyeStatus)newEyeStatus
{
    
    //__block CDVPluginResult* result = nil;
    NSLog(@"%li", (long)newEyeStatus);
    //self.scanningOverlay.targetHighlighted = NO;
    switch (newEyeStatus) {
        case EVEyeStatusTooClose:{
            NSLog(@"%@", @"Eyes coo close");
            
            message.text = @"Eyes coo close";
            //EyeVerify *ev = [EyeVerifyLoader getEyeVerifyInstance];
            
            //[ev continueAuth];
            
            //result = [CDVPluginResult resultWithStatus:CDVCommandStatus_INVALID_ACTION messageAsString:@"Position your eyes in front of front camera (about 20cm to device)"];
            //EyeVerify *ev = [EyeVerifyLoader getEyeVerifyInstance];
            //[ev cancel];
            //if (result) {
            //    [self.commandDelegate sendPluginResult:result callbackId:_latestCommand.callbackId];
            //}
            //self.hasPendingOperation = NO;
    }
    break;
        case EVEyeStatusNoEye:{
            NSLog(@"%@", @"Position your eyes in the window");
            
            message.text = @"Position your eyes in front of front camera (about 20cm to device)";
            //EyeVerify *ev = [EyeVerifyLoader getEyeVerifyInstance];
            
            //[ev continueAuth];

            //result = [CDVPluginResult resultWithStatus:CDVCommandStatus_INVALID_ACTION messageAsString:@"Position your eyes in front of front camera (about 20cm to device)"];
            //EyeVerify *ev = [EyeVerifyLoader getEyeVerifyInstance];
            //[ev cancel];
            //if (result) {
            //    [self.commandDelegate sendPluginResult:result callbackId:_latestCommand.callbackId];
            //}
            //self.hasPendingOperation = NO;
        }
            break;
        case EVEyeStatusTooFar:{
            NSLog(@"%@", @"Move device closer");
            message.text = @"Move device closer (about 20cm to device)";
            //EyeVerify *ev = [EyeVerifyLoader getEyeVerifyInstance];
            
            //[ev continueAuth];

            /*result = [CDVPluginResult resultWithStatus:CDVCommandStatus_INVALID_ACTION messageAsString:@"Move device closer (about 20cm to device)"];
            EyeVerify *ev = [EyeVerifyLoader getEyeVerifyInstance];
            [ev cancel];
            if (result) {
                [self.commandDelegate sendPluginResult:result callbackId:_latestCommand.callbackId];
            }
            self.hasPendingOperation = NO;*/
        }
            break;
        case EVEyeStatusOkay:
            NSLog(@"%@", @"Scanning OK");
            message.text = @"Processing...";

            break;
    }
}

- (void) enrollmentProgressUpdated:(float)completionRatio counter:(int)counterValue
{
    NSLog(@"counter: %d  completionRatio: %f",counterValue, completionRatio);
    progress.progress = completionRatio;
    counter.text = [NSString stringWithFormat:@"%i", counterValue];
    
}

- (void) enrollmentSessionStarted:(int)totalSteps
{
    NSLog(@"totalSteps: %d ",totalSteps);
    scanOverlay.hidden = NO;

}

- (void)enrollmentSessionCompleted:(BOOL)isFinished
{
    NSLog(@"isFinished: %d ",isFinished);
    if(!isFinished)
    {
        EyeVerify *ev = [EyeVerifyLoader getEyeVerifyInstance];

        [ev continueAuth];
        
    }
}


-(void)clearUI:(CDVInvokedUrlCommand *)command
{
    self.hasPendingOperation = YES;
    self.latestCommand = command;
    [[self.viewController.view viewWithTag:990] removeFromSuperview];
    [message removeFromSuperview];
    [progress removeFromSuperview];
    [counter removeFromSuperview];
    [scanOverlay removeFromSuperview];
    
    EyeVerify *ev = [EyeVerifyLoader getEyeVerifyInstance];
    if (ev) {
        [ev cancel];
    }
}



@end
