// Elucideye, Inc. ("COMPANY") CONFIDENTIAL
// Unpublished Copyright (c) 2015 Elucideye, Inc.
// All Rights Reserved.
//
// NOTICE: All information contained herein is, and remains the
// property of COMPANY. The intellectual and technical concepts
// contained herein are proprietary to COMPANY and may be covered by
// U.S. and Foreign Patents, patents in process, and are protected by
// trade secret or copyright law.  Dissemination of this information
// or reproduction of this material is strictly forbidden unless prior
// written permission is obtained from COMPANY.  Access to the source
// code contained herein is hereby forbidden to anyone except current
// COMPANY employees, managers or contractors who have executed
// Confidentiality and Non-disclosure agreements explicitly covering
// such access.
//
// The copyright notice above does not evidence any actual or intended
// publication or disclosure of this source code, which includes
// information that is confidential and/or proprietary, and is a trade
// secret, of COMPANY.  ANY REPRODUCTION, MODIFICATION, DISTRIBUTION,
// PUBLIC PERFORMANCE, OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS
// SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF COMPANY IS
// STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE LAWS AND
// INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF THIS SOURCE
// CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
// TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO
// MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE
// OR IN PART.
//
//  DrishtiEyeSegmenter.hpp
//  drishtisdk

#ifndef __drishtisdk__DrishtiEyeSegmenter__
#define __drishtisdk__DrishtiEyeSegmenter__

#include "drishti/drishti_sdk.hpp" // TODO: get rid of this
#include "drishti/Image.hpp"
#include "drishti/Eye.hpp"

#include <memory> // unique_ptr, shared_ptr
#include <vector> // for eyelid contour

_DRISHTI_SDK_BEGIN

/*
 * DrishtiEyeSegmenter
 */

class DRISHTI_EXPORTS EyeSegmenter
{
public:
    
    class Impl;
    EyeSegmenter(const std::string &filename);
    EyeSegmenter(std::istream &is);
    
    // EyeSegmenter cannot be moved or copied:
    EyeSegmenter(const EyeSegmenter &) = delete;
    EyeSegmenter& operator=(const EyeSegmenter &) = delete;
    EyeSegmenter(EyeSegmenter &&) = delete;
    
    ~EyeSegmenter();
    
    int operator()(const Image3b &image, Eye &eye, bool isRight);
    Eye getMeanEye(int width) const;
    
    void setEyelidInits(int count);
    int getEyelidInits() const;
    
    void setIrisInits(int count);
    int getIrisInits() const;
    
    int getMinWidth() const;
    
    void setOptimizationLevel(int level);
    
    // Aspect ratio as width/height
    float getRequiredAspectRatio() const;
    
protected:
    
    void init(std::istream &is);
    std::unique_ptr<Impl> m_impl;
};

_DRISHTI_SDK_END

/*
 * Extern "C" interface (dlopen/dlsym)
 */

DRISHTI_EXTERN_C_BEGIN
DRISHTI_EXPORTS drishti::sdk::EyeSegmenter* drishti_create_from_file(const std::string &filename);
DRISHTI_EXPORTS drishti::sdk::EyeSegmenter* drishti_create_from_stream(std::istream &is);
DRISHTI_EXPORTS void drishti_destroy(drishti::sdk::EyeSegmenter *segmenter);
DRISHTI_EXTERN_C_END

#endif /* defined(__drishtisdk__DrishtiEyeSegmenter__) */
