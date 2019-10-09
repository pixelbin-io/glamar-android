#include <jni.h>
//#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>
//#include <opencv2/imgcodecs.hpp>
#include "common-utilities.h"
#include "blushing.hpp"
#include "spline.hpp"
#include <vector>
#include <algorithm>
#include <android/log.h>
#include "apply-makeup.h"

#define  LOG_TAG    "someTag"

enum MakeupType{lipstick, blush, eyeliner, eyeshadow};
double gamma_coefficient = 0.5;
bool applyGammaCorrection = false;

#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

#define JNI_METHOD(NAME) \
    Java_com_fynd_artoolkit_java_facedetection_FaceContourDetectorProcessor_##NAME



extern "C"
JNIEXPORT void JNICALL
JNI_METHOD(AugmentFace)(JNIEnv *env, jobject instance,jlong matAddrInput, jlong matAddrResult,
                             jfloatArray faceX, jfloatArray faceY,
                             jfloatArray leftEyebrowTopX, jfloatArray leftEyebrowTopY,
                             jfloatArray leftEyebrowBottomX, jfloatArray leftEyebrowBottomY,
                             jfloatArray rightEyebrowTopX, jfloatArray rightEyebrowTopY,
                             jfloatArray rightEyebrowBottomX, jfloatArray rightEyebrowBottomY,
                             jfloatArray leftEyeX, jfloatArray leftEyeY, jfloatArray rightEyeX, jfloatArray rightEyeY,
                             jfloatArray noseBridgeX, jfloatArray noseBridgeY, jfloatArray noseBottomX, jfloatArray noseBottomY,
                             jfloatArray upperLipTopX, jfloatArray upperLipTopY,
                             jfloatArray upperLipBottomX, jfloatArray upperLipBottomY,
                             jfloatArray lowerLipTopX, jfloatArray lowerLipTopY,
                             jfloatArray lowerLipBottomX, jfloatArray lowerLipBottomY,
                             jint selectedColorR, jint selectedColorG, jint selectedColorB,
                             jint selectedMakeupIndex, jfloat colorAlpha) {

    jfloat* jfaceX = env->GetFloatArrayElements( faceX,0);
    jfloat* jfaceY = env->GetFloatArrayElements( faceY,0);
    jfloat* jleftEyebrowTopX = env->GetFloatArrayElements( leftEyebrowTopX,0);
    jfloat* jleftEyebrowTopY = env->GetFloatArrayElements( leftEyebrowTopY,0);
    jfloat* jleftEyebrowBottomX = env->GetFloatArrayElements( leftEyebrowBottomX,0);
    jfloat* jleftEyebrowBottomY = env->GetFloatArrayElements( leftEyebrowBottomY,0);
    jfloat* jrightEyebrowTopX = env->GetFloatArrayElements( rightEyebrowTopX,0);
    jfloat* jrightEyebrowTopY = env->GetFloatArrayElements( rightEyebrowTopY,0);
    jfloat* jrightEyebrowBottomX = env->GetFloatArrayElements( rightEyebrowBottomX,0);
    jfloat* jrightEyebrowBottomY = env->GetFloatArrayElements( rightEyebrowBottomY,0);
    jfloat* jleftEyeX = env->GetFloatArrayElements( leftEyeX,0);
    jfloat* jleftEyeY = env->GetFloatArrayElements( leftEyeY,0);
    jfloat* jrightEyeX = env->GetFloatArrayElements( rightEyeX,0);
    jfloat* jrightEyeY = env->GetFloatArrayElements( rightEyeY,0);
    jfloat* jnoseBridgeX = env->GetFloatArrayElements( noseBridgeX,0);
    jfloat* jnoseBridgeY = env->GetFloatArrayElements( noseBridgeY,0);
    jfloat* jnoseBottomX = env->GetFloatArrayElements( noseBottomX,0);
    jfloat* jnoseBottomY = env->GetFloatArrayElements( noseBottomY,0);
    jfloat* jupperLipTopX = env->GetFloatArrayElements( upperLipTopX,0);
    jfloat* jupperLipTopY = env->GetFloatArrayElements( upperLipTopY,0);
    jfloat* jupperLipBottomX = env->GetFloatArrayElements( upperLipBottomX,0);
    jfloat* jupperLipBottomY = env->GetFloatArrayElements( upperLipBottomY,0);
    jfloat* jlowerLipTopX = env->GetFloatArrayElements( lowerLipTopX,0);
    jfloat* jlowerLipTopY = env->GetFloatArrayElements( lowerLipTopY,0);
    jfloat* jlowerLipBottomX = env->GetFloatArrayElements( lowerLipBottomX,0);
    jfloat* jlowerLipBottomY = env->GetFloatArrayElements( lowerLipBottomY,0);


    // TODO
    // Convert input RGBA image to GRAY image
    cv::Mat &matInput = *(cv::Mat *) matAddrInput;
    cv::Mat &matResult = *(cv::Mat *) matAddrResult;

    cvtColor(matInput, matInput, cv::COLOR_BGRA2BGR);

    // Get size of landmark points
    int ult_len =  env->GetArrayLength(upperLipTopX);
    int ulb_len = env->GetArrayLength(upperLipBottomX);
    /*
     * The length of points in lower lips is manually increased by 2
     * because MLKit does not include end points of curves which make the lower lips
     * we need to include these two extreme points from upper lip.
     */
    int llb_len = env->GetArrayLength(lowerLipBottomX) + 2 ;
    int llt_len = env->GetArrayLength(lowerLipTopX) + 2 ;
    int eye_len = env->GetArrayLength(leftEyeX);
    int eyebrow_len = env->GetArrayLength(leftEyebrowBottomX) ;
    int face_len = env->GetArrayLength(faceX) ;


    // convert RGB color values to cv::Scalar
    jint* jselectedColorR = &selectedColorR;
    jint* jselectedColorG = &selectedColorG;
    jint* jselectedColorB = &selectedColorB;
    cv::Scalar selectedColor(*jselectedColorR, *jselectedColorG, *jselectedColorB);

    /*
     * The makeupID should be a value corresponding to one of the entry in enumerated type "makeupID".
     * Care must be taken while defining the same enum type in Java/kotlin files. You must ensure that
     * each of the identical makeup type has the identical value both in the c++ files and Java/Kotlin.
     */
    int makeupID = selectedMakeupIndex;
    float alpha = colorAlpha;       // alpha (range = [0,1]) decides the darkness of a color

    switch(makeupID)
    {
        case MakeupType::lipstick : {
            // Build vector of cv::Points from available native arrays
            std::vector<cv::Point> ult_points(ult_len),ulb_points(ulb_len),llb_points(llb_len),llt_points(llt_len);
            std::vector<cv::Point> ult_c,ulb_c,llt_c,llb_c;

            for(int index = 0 ; index < ult_len ; index++){  ult_points[index]  = cv::Point(jupperLipTopX[index],jupperLipTopY[index]); }
            for(int index = 0 ; index < ulb_len ; index++){ ulb_points[index]  = cv::Point(jupperLipBottomX[index],jupperLipBottomY[index]);  }

            /*
             * While filling the arrays for lower lips, care must taken for the extreme points
             * which need to be filled using value from upper lip coordinates
             */
            llt_points[0] = ulb_points[ulb_len - 1];
            for(int index = 1 ; index <= llt_len-2 ; index++){ llt_points[index]  = cv::Point(jlowerLipTopX[index-1],jlowerLipTopY[index-1]);  }
            llt_points[llt_len-1] = ulb_points[0];

            llb_points[ 0 ] = ult_points[ult_len -1];
            for(int index = 1 ; index <= llb_len-2 ; index++){ llb_points[index]  = cv::Point(jlowerLipBottomX[index-1],jlowerLipBottomY[index-1]); }
            llb_points[llb_len - 1] = ult_points[0];

            // Interpolate lip curves
            ult_c = tinyspline_cubic_interpolation(ult_points);
            ulb_c = tinyspline_cubic_interpolation(ulb_points);
            llt_c = tinyspline_cubic_interpolation(llt_points);
            llb_c = tinyspline_cubic_interpolation(llb_points);

            apply_lipstick(matInput, selectedColor,alpha, ult_c, ulb_c,llt_c, llb_c );
            break;
        }
            case MakeupType::blush :{

                /* Calculate ellipse parameters for both left and right cheek */
                std::vector<cv::Point> face_points(face_len);
                for(int index = 0 ; index < face_len ; index++){  face_points[index]  = cv::Point(jfaceX[index],jfaceY[index]); }
                bool apply_left_blush = true,apply_right_blush = true;
                int tmpDistance_1 , tmpDistance_2;

             // left cheek
            int left_cheek_center_x =  (int) (jleftEyeX[15] + jupperLipTopX[0] + jnoseBottomX[0] + jfaceX[28] +  jfaceX[25] + jnoseBottomX[0] - jnoseBottomX[2]) / 5;
            int left_cheek_center_y = (int) (jleftEyeY[15] + jupperLipTopY[0] + jnoseBottomY[0] + jfaceY[28] +  jfaceY[25]) / 5;
            int semi_minor_axis = (int) cv::norm(cv::Point((int) jfaceX[25], (int) jfaceY[25]) - cv::Point(left_cheek_center_x, left_cheek_center_y)) / 2;
            int semi_major_axis =  (int)( cv::norm(cv::Point(jupperLipTopX[0],jupperLipTopY[0]) - cv::Point((int) jfaceX[27], (int) jfaceY[27]))*0.40);
            int left_rotation_angle = 50;
            int left_start_angle = 0;
            int left_end_angle = 360;
            cv::Point left_center = cv::Point(left_cheek_center_x, left_cheek_center_y);
            cv::Size left_blush_size(semi_minor_axis, semi_major_axis);
            tmpDistance_1 = (int)( cv::norm( cv::Point((int) jleftEyeX[0], (int) jleftEyeY[0])  -  cv::Point((int) jleftEyeX[8], (int) jleftEyeY[8]) ) * 0.5 );
            tmpDistance_2 = (int) cv::norm( cv::Point((int) jfaceX[29], (int) jfaceY[29])  -  cv::Point((int) jleftEyeX[0], (int) jleftEyeY[0]) );
            if (tmpDistance_1 > tmpDistance_2) { apply_left_blush = false; };

            // right cheek
            int right_cheek_center_x =  (int) (jrightEyeX[8] + jupperLipTopX[10] + jnoseBottomX[2] + jfaceX[8] + jfaceX[11] - (jnoseBottomX[0] - jnoseBottomX[2])) / 5;
            int right_cheek_center_y =  (int) (jrightEyeY[8] + jupperLipTopY[10] + jnoseBottomY[2] + jfaceY[8] + jfaceY[11]) / 5;
            int right_semi_minor_axis = (int) cv::norm(cv::Point((int) jfaceX[11], (int) jfaceY[11]) - cv::Point(right_cheek_center_x, right_cheek_center_y)) / 2;
            int right_semi_major_axis =  (int)( cv::norm(cv::Point(jupperLipTopX[10],jupperLipTopY[10]) - cv::Point((int) jfaceX[7], (int) jfaceY[7]))*0.25);
            int right_rotation_angle = 150;
            int right_start_angle = 0;
            int right_end_angle = 360;
            cv::Point right_center = cv::Point(right_cheek_center_x, right_cheek_center_y);
            cv::Size right_blush_size(right_semi_minor_axis, right_semi_major_axis);
            tmpDistance_1 = (int) (cv::norm( cv::Point((int) jrightEyeX[0], (int) jrightEyeY[0])  -  cv::Point((int) jrightEyeX[8], (int) jrightEyeY[8]) ) * 0.5 );
            tmpDistance_2 = (int) cv::norm( cv::Point((int) jfaceX[7], (int) jfaceY[7])  -  cv::Point((int) jrightEyeX[8], (int) jrightEyeY[8]) );
            if (tmpDistance_1 > tmpDistance_2) { apply_right_blush = false; };


                apply_blush(matInput, selectedColor ,alpha,
                        left_center , left_blush_size  ,  left_rotation_angle , left_start_angle, left_end_angle, apply_left_blush,
                        right_center, right_blush_size , right_rotation_angle, right_start_angle, right_end_angle, apply_right_blush,
                        face_points);
            break;
        }
        case MakeupType::eyeliner: {
            /*
             * Get eye points for both eyes and  eyebrows.
             * "Shift" can be used for eyes to shift the upper curve up by "shift" points amd
             * lower curves down by same length. The other more precise method would be to use
             * the function rescale_curve.
             * Corner Shift can be used to shift the corner points of eyes
             */
            int index = 0;
            int shift = 0;
            int corner_shift = 0;
            std::vector<cv::Point> leftEye(eye_len);
            std::vector<cv::Point> rightEye(eye_len);
            std::vector<cv::Point> right_eyebrow_b(eyebrow_len), left_eyebrow_b(eyebrow_len);

            index = 0;
            leftEye[index] = cv::Point(jleftEyeX[index], jleftEyeY[index] - corner_shift);
            rightEye[index] = cv::Point(jrightEyeX[index], jrightEyeY[index] - corner_shift);

            for (index = 1; index <= 7; index++) {
                leftEye[index] = cv::Point(jleftEyeX[index], jleftEyeY[index] - shift);
                rightEye[index] = cv::Point(jrightEyeX[index], jrightEyeY[index] - shift);
            }

            index = 8;
            leftEye[index] = cv::Point(jleftEyeX[index], jleftEyeY[index] - corner_shift);
            rightEye[index] = cv::Point(jrightEyeX[index], jrightEyeY[index] - corner_shift);

            for (index = 9; index <= 15; index++) {
                leftEye[index] = cv::Point(jleftEyeX[index], jleftEyeY[index] + shift);
                rightEye[index] = cv::Point(jrightEyeX[index], jrightEyeY[index] + shift);
            }

            for (index = 0; index < eyebrow_len; index++) {
                right_eyebrow_b[index] = cv::Point(jrightEyebrowBottomX[index],
                                                   jrightEyebrowBottomY[index]);
                left_eyebrow_b[index] = cv::Point(jleftEyebrowBottomX[index],
                                                  jleftEyebrowBottomY[index]);
            }
            apply_eyeLiner(matInput, selectedColor, alpha,leftEye, rightEye);
            break;
        }
        case MakeupType ::eyeshadow: {
            int index = 0;
            int shift = 2;
            int corner_shift = 0;
            std::vector<cv::Point> leftEye(eye_len);
            std::vector<cv::Point> rightEye(eye_len);
            std::vector<cv::Point> right_eyebrow_b(eyebrow_len), left_eyebrow_b(eyebrow_len);

            leftEye[index] = cv::Point(jleftEyeX[index], jleftEyeY[index] - corner_shift);
            rightEye[index] = cv::Point(jrightEyeX[index], jrightEyeY[index] - corner_shift);

            for (index = 1; index <= 7; index++) {
                leftEye[index] = cv::Point(jleftEyeX[index], jleftEyeY[index] - shift);
                rightEye[index] = cv::Point(jrightEyeX[index], jrightEyeY[index] - shift);
            }

            leftEye[index] = cv::Point(jleftEyeX[index], jleftEyeY[index] - corner_shift);
            rightEye[index] = cv::Point(jrightEyeX[index], jrightEyeY[index] - corner_shift);

            for (index = 9; index <= 15; index++) {
                leftEye[index] = cv::Point(jleftEyeX[index], jleftEyeY[index] + shift);
                rightEye[index] = cv::Point(jrightEyeX[index], jrightEyeY[index] + shift);
            }

            for (index = 0; index < eyebrow_len; index++) {
                right_eyebrow_b[index] = cv::Point(jrightEyebrowBottomX[index],
                                                   jrightEyebrowBottomY[index]);
                left_eyebrow_b[index] = cv::Point(jleftEyebrowBottomX[index],
                                                  jleftEyebrowBottomY[index]);
            }
            apply_eyeshadow(matInput,  selectedColor, alpha ,
                           leftEye, rightEye,
                           right_eyebrow_b, left_eyebrow_b);
            break;
        }
        default : {
            break;
        }
    }

    // Gamma correction controls the overall brightness of the image
    if(applyGammaCorrection) {
        gammaCorrection(matInput, gamma_coefficient);
    }

    matResult = matInput;

    env->ReleaseFloatArrayElements(faceX, jfaceX, 0);
    env->ReleaseFloatArrayElements(faceY, jfaceY, 0);
    env->ReleaseFloatArrayElements(leftEyebrowTopX, jleftEyebrowTopX, 0);
    env->ReleaseFloatArrayElements(leftEyebrowTopY, jleftEyebrowTopY, 0);
    env->ReleaseFloatArrayElements(leftEyebrowBottomX, jleftEyebrowBottomX, 0);
    env->ReleaseFloatArrayElements(leftEyebrowBottomY, jleftEyebrowBottomY, 0);
    env->ReleaseFloatArrayElements(rightEyebrowTopX, jrightEyebrowTopX, 0);
    env->ReleaseFloatArrayElements(rightEyebrowTopY, jrightEyebrowTopY, 0);
    env->ReleaseFloatArrayElements(rightEyebrowBottomX, jrightEyebrowBottomX, 0);
    env->ReleaseFloatArrayElements(rightEyebrowBottomY, jrightEyebrowBottomY, 0);
    env->ReleaseFloatArrayElements(leftEyeX, jleftEyeX, 0);
    env->ReleaseFloatArrayElements(leftEyeY, jleftEyeY, 0);
    env->ReleaseFloatArrayElements(rightEyeX, jrightEyeX, 0);
    env->ReleaseFloatArrayElements(rightEyeY, jrightEyeY, 0);
    env->ReleaseFloatArrayElements(noseBridgeX, jnoseBridgeX, 0);
    env->ReleaseFloatArrayElements(noseBridgeY, jnoseBridgeY, 0);
    env->ReleaseFloatArrayElements(noseBottomX, jnoseBottomX, 0);
    env->ReleaseFloatArrayElements(noseBottomY, jnoseBottomY, 0);
    env->ReleaseFloatArrayElements(upperLipTopX, jupperLipTopX, 0);
    env->ReleaseFloatArrayElements(upperLipTopY, jupperLipTopY, 0);
    env->ReleaseFloatArrayElements(upperLipBottomX, jupperLipBottomX, 0);
    env->ReleaseFloatArrayElements(upperLipBottomY, jupperLipBottomY, 0);
    env->ReleaseFloatArrayElements(lowerLipTopX, jlowerLipTopX, 0);
    env->ReleaseFloatArrayElements(lowerLipTopY, jlowerLipTopY, 0);
    env->ReleaseFloatArrayElements(lowerLipBottomX, jlowerLipBottomX, 0);
    env->ReleaseFloatArrayElements(lowerLipBottomY, jlowerLipBottomY, 0);


}