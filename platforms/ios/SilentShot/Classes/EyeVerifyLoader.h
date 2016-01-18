//
//  EyeVerifyLoader.h
//  EyeprintID
//

#import <Cordova/CDVPlugin.h>

#import <EyeVerify/EyeVerify.h>

#import <Foundation/Foundation.h>

@interface EyeVerifyLoader : NSObject

- (void)loadEyeVerifyWithLicense:(NSString *)license;
+ (EyeVerify *)getEyeVerifyInstance;

@end
