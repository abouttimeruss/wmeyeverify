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
//  Eye.hpp
//  drishtisdk

#ifndef __drishtisdk__Eye__
#define __drishtisdk__Eye__

#include "drishti/drishti_sdk.hpp"
#include "drishti/drishti_sdk.hpp"
#include "drishti/Image.hpp"

#include <vector>
#include <iostream>

_DRISHTI_SDK_BEGIN

/*
 * Eye type
 */

class DRISHTI_EXPORTS Eye
{
public:
    struct Ellipse
    {
        Vec2f center = {0.f,0.f};
        Size2f size = {0.f,0.f};
        float angle = 0.f;
    };
    
    Eye();
    Eye(const Eye &src);
    
    void setIris(const Ellipse &src) { iris = src; }
    void setPupil(const Ellipse &src) { pupil = src; }
    void setEyelids(const std::vector<Vec2f>& src) { eyelids = src; }
    void setCrease(const std::vector<Vec2f>& src) { crease = src; }
    void setCorners(const Vec2f &inner, const Vec2f &outer) { innerCorner = inner; outerCorner = outer; }
    void setRoi(const Recti &src) { roi = src; }
    
    const Ellipse& getIris() const { return iris; }
    Ellipse& getIris() { return iris; }
    
    const Ellipse& getPupil() const { return pupil; }
    Ellipse& getPupil() { return pupil; }
    
    const std::vector<Vec2f> &getEyelids() const { return eyelids; }
    std::vector<Vec2f> &getEyelids() { return eyelids; }
    
    const std::vector<Vec2f> &getCrease() const { return crease; }
    std::vector<Vec2f> &getCrease() { return crease; }
    
    const Vec2f & getInnerCorner() const { return innerCorner; }
    Vec2f & getInnerCorner() { return innerCorner; }
    
    const Vec2f & getOuterCorner() const { return outerCorner; }
    Vec2f & getOuterCorner() { return outerCorner; }
    
    const Recti & getRoi() const { return roi; }
    Recti & getRoi() { return roi; }
    
protected:
    
    Ellipse iris;
    Ellipse pupil;
    std::vector<Vec2f> eyelids;
    std::vector<Vec2f> crease;
    Vec2f innerCorner;
    Vec2f outerCorner;
    Recti roi;
};

struct DRISHTI_EXPORTS EyeStream
{
    enum Format { XML, JSON };
    EyeStream(const Format &format) : format(format) {}
    std::string ext() const;
    Format format = XML;
};

struct DRISHTI_EXPORTS EyeOStream : public EyeStream
{
    EyeOStream(const Eye &eye, Format format) : EyeStream(format), eye(eye) {}
    const Eye &eye;
};

struct DRISHTI_EXPORTS EyeIStream : public EyeStream
{
    EyeIStream(Eye &eye, Format format) : EyeStream(format), eye(eye) {}
    Eye &eye;
};

std::ostream& operator<<(std::ostream &os, const EyeOStream &eye) DRISHTI_EXPORTS;
std::istream& operator>>(std::istream &is, EyeIStream &eye) DRISHTI_EXPORTS;

enum EyeRegions
{
    kScleraRegion  = 1,
    kIrisRegion    = 2,
    kPupilRegion   = 4
};

void DRISHTI_EXPORTS createMask(Image1b &mask, const Eye &eye, int components=kIrisRegion);

_DRISHTI_SDK_END

#endif // __drishtisdk__Eye__

