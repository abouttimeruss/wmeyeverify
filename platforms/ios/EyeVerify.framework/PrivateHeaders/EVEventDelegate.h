//===-- EVEventDelegate.h - EVEventDelegate class definition --------------===//
//
//                     EyeVerify Codebase
//
//===----------------------------------------------------------------------===//
///
/// @file
/// @brief This file contains the declaration of the EVEventDelegate class.
///
//===----------------------------------------------------------------------===//

#import "EVAuthenticatorDelegate.h"
#import "EVVideoCameraDelegate.h"
#import "EvpEventDelegate.hpp"
#import "EVAudioLivenessDelegate.h"

#import <Foundation/Foundation.h>

@interface EVEventDelegate : NSObject

@property (nonatomic, weak) id<EVVideoCameraDelegate> videoCameraDelegate;
@property (nonatomic, weak) id<EVAuthenticatorDelegate> evAuthenticatorDelegate;
@property (nonatomic, weak) id<EVAudioLivenessDelegate> evAudioLivenessDelegate;
@property (nonatomic) EvpEventDelegate *evpEventDelegate;

+ (EVEventDelegate *)getInstance;

- (void)publishEventFocusCompleted;
- (void)publishEventExposureCompleted;
- (void)publishEventRecordedAudio:(const std::vector<short> &)samples;
- (void)publishEventRecordingAudioStarted;

- (void)targetMovementCompleted:(BOOL)isVisible atPoint:(CGPoint)point animationDuration:(NSTimeInterval)duration;

@end
