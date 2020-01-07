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

#include "opencv2/imgcodecs.hpp"
#include "opencv2/highgui.hpp"
#include <iostream>

#include "apply-makeup.h"

#define  LOG_TAG    "someTag"

enum MakeupType{lipstick, blush, eyeliner, eyeshadow,kajal};
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
            if(!face_points.empty()) {
                bool apply_left_blush = true, apply_right_blush = true;
                int tmpDistance_1, tmpDistance_2;

                // left cheek
                int left_cheek_center_x =
                        (int) (jleftEyeX[15] + jupperLipTopX[0] + jnoseBottomX[0] + jfaceX[28] +
                               jfaceX[25] + jnoseBottomX[0] - jnoseBottomX[2]) / 5;
                int left_cheek_center_y =
                        (int) (jleftEyeY[15] + jupperLipTopY[0] + jnoseBottomY[0] + jfaceY[28] +
                               jfaceY[25]) / 5;
                int semi_minor_axis = (int) cv::norm(cv::Point((int) jfaceX[25], (int) jfaceY[25]) -
                                                     cv::Point(left_cheek_center_x,
                                                               left_cheek_center_y)) / 2;
                int semi_major_axis = (int) (cv::norm(
                        cv::Point(jupperLipTopX[0], jupperLipTopY[0]) -
                        cv::Point((int) jfaceX[27], (int) jfaceY[27])) * 0.40);
                int left_rotation_angle = 50;
                int left_start_angle = 0;
                int left_end_angle = 360;
                cv::Point left_center = cv::Point(left_cheek_center_x, left_cheek_center_y);
                cv::Size left_blush_size(semi_minor_axis, semi_major_axis);
                tmpDistance_1 = (int) (cv::norm(cv::Point((int) jleftEyeX[0], (int) jleftEyeY[0]) -
                                                cv::Point((int) jleftEyeX[8], (int) jleftEyeY[8])) *
                                       0.5);
                tmpDistance_2 = (int) cv::norm(cv::Point((int) jfaceX[29], (int) jfaceY[29]) -
                                               cv::Point((int) jleftEyeX[0], (int) jleftEyeY[0]));
                if (tmpDistance_1 > tmpDistance_2) { apply_left_blush = false; };

                // right cheek
                int right_cheek_center_x =
                        (int) (jrightEyeX[8] + jupperLipTopX[10] + jnoseBottomX[2] + jfaceX[8] +
                               jfaceX[11] - (jnoseBottomX[0] - jnoseBottomX[2])) / 5;
                int right_cheek_center_y =
                        (int) (jrightEyeY[8] + jupperLipTopY[10] + jnoseBottomY[2] + jfaceY[8] +
                               jfaceY[11]) / 5;
                int right_semi_minor_axis = (int) cv::norm(
                        cv::Point((int) jfaceX[11], (int) jfaceY[11]) -
                        cv::Point(right_cheek_center_x, right_cheek_center_y)) / 2;
                int right_semi_major_axis = (int) (cv::norm(
                        cv::Point(jupperLipTopX[10], jupperLipTopY[10]) -
                        cv::Point((int) jfaceX[7], (int) jfaceY[7])) * 0.25);
                int right_rotation_angle = 150;
                int right_start_angle = 0;
                int right_end_angle = 360;
                cv::Point right_center = cv::Point(right_cheek_center_x, right_cheek_center_y);
                cv::Size right_blush_size(right_semi_minor_axis, right_semi_major_axis);
                tmpDistance_1 = (int) (cv::norm(
                        cv::Point((int) jrightEyeX[0], (int) jrightEyeY[0]) -
                        cv::Point((int) jrightEyeX[8], (int) jrightEyeY[8])) * 0.5);
                tmpDistance_2 = (int) cv::norm(cv::Point((int) jfaceX[7], (int) jfaceY[7]) -
                                               cv::Point((int) jrightEyeX[8], (int) jrightEyeY[8]));
                if (tmpDistance_1 > tmpDistance_2) { apply_right_blush = false; };


                apply_blush(matInput, selectedColor, alpha,
                            left_center, left_blush_size, left_rotation_angle, left_start_angle,
                            left_end_angle, apply_left_blush,
                            right_center, right_blush_size, right_rotation_angle, right_start_angle,
                            right_end_angle, apply_right_blush,
                            face_points);
                break;
            }
        }
        case MakeupType  ::kajal:{
            int index = 0;
            int shift = 0;
            int corner_shift = 0;
//            std::vector<cv::Point> leftupEye(8);
//            std::vector<cv::Point> leftdownEye(8);
            std::vector<cv::Point> leftkajalEye(17);
            std::vector<cv::Point> rightkajalEye(17);
            cv ::Point rook_points[2][20];

            //cv::Mat res = matInput.clone();


//            for (index = 0; index <= 16 ;index++) {
//                if(index==0){
//                    leftkajalEye[index] = cv::Point(jleftEyeX[index+8], jleftEyeY[index+8] );
//                    rightkajalEye[index] = cv::Point(jleftEyeX[index+8], jleftEyeY[index+8] );
//                }
//                else if(index<=7 && index>0){
//                    leftkajalEye[index] = cv::Point(jleftEyeX[index+8], jleftEyeY[index+8] );
//                }
//                else if(index==8){
//                    leftkajalEye[index] = cv::Point(jleftEyeX[0], jleftEyeY[0] );
//                }
//                else if(index==9){
//                    leftkajalEye[index] = cv::Point(jleftEyeX[0], jleftEyeY[0] + 3);
//                }
//                else if(index==10){
//                    leftkajalEye[index] = cv::Point(jleftEyeX[15], jleftEyeY[15] +4);
//                }
//                else if(index==11){
//                    leftkajalEye[index] = cv::Point(jleftEyeX[14], jleftEyeY[14] +4);
//                }
//                else if(index==12){
//                    leftkajalEye[index] = cv::Point(jleftEyeX[13], jleftEyeY[13] +3);
//                }
//                else if(index==13){
//                    leftkajalEye[index] = cv::Point(jleftEyeX[12], jleftEyeY[12] +2);
//                }
//                else if(index==14){
//                    leftkajalEye[index] = cv::Point(jleftEyeX[11], jleftEyeY[11] +2);
//                }
//                else if(index==15){
//                    leftkajalEye[index] = cv::Point(jleftEyeX[10], jleftEyeY[10] +1);
//                }
//                else if(index==16){
//                    leftkajalEye[index] = cv::Point(jleftEyeX[9], jleftEyeY[9] +1);
//                }
//
//
//
//            }



            index = 0;
            for (index = 0; index <= 20; index++) {
                if(index==0){
                    rook_points[0][index] = cv::Point(jleftEyeX[index+8], jleftEyeY[index+8]-1 );


                }
                else if(index<=7 && index>0){
                    rook_points[0][index] = cv::Point(jleftEyeX[index+8], jleftEyeY[index+8]-1 );

                }
                else if(index==8){
                    rook_points[0][index] = cv::Point(jleftEyeX[0], jleftEyeY[0] );

                }
                else if(index==9){
                    rook_points[0][index] = cv::Point(2*jleftEyeX[0]-jleftEyeX[15], jleftEyeY[1] );
                }
//                else if(index==10){
//                    rook_points[0][index] = cv::Point(2*jleftEyeX[0]-jleftEyeX[15], jleftEyeY[2] );
//                }
//                else if(index==11){
//                    rook_points[0][index] = cv::Point(2*jleftEyeX[0]-jleftEyeX[15], jleftEyeY[2]+1 );
//                }
                else if(index==10){
                    rook_points[0][index] = cv::Point(2*jleftEyeX[0]-jleftEyeX[15], jleftEyeY[1]+1 );
                }

                else if(index==11){
                    rook_points[0][index] = cv::Point(jleftEyeX[0], jleftEyeY[0] + 3);

                }
                else if(index==12){
                    rook_points[0][index] = cv::Point(jleftEyeX[15], jleftEyeY[15] +3);

                }
                else if(index==13){
                    rook_points[0][index] = cv::Point(jleftEyeX[14], jleftEyeY[14] +3);

                }
                else if(index==14){
                    rook_points[0][index] = cv::Point(jleftEyeX[13], jleftEyeY[13] +2);

                }
                else if(index==15){
                    rook_points[0][index] = cv::Point(jleftEyeX[12], jleftEyeY[12] +1);

                }
                else if(index==16){
                    rook_points[0][index] = cv::Point(jleftEyeX[11], jleftEyeY[11] +1);

                }
                else if(index==17){
                    rook_points[0][index] = cv::Point(jleftEyeX[10], jleftEyeY[10] +1);

                }
                else if(index==18){
                    rook_points[0][index] = cv::Point(jleftEyeX[9], jleftEyeY[9] +1);

                }
                else if(index==19){
                    rook_points[0][index] = cv::Point(jleftEyeX[8], jleftEyeY[8] +1);

                }




            }

            index = 0;
            for (index = 0; index <= 20; index++) {

                if(index<=7 && index>=0){

                    rook_points[1][index] = cv::Point(jrightEyeX[index+8], jrightEyeY[index+8]-1 );
                }
                else if(index==8){

                    rook_points[1][index] = cv::Point(jrightEyeX[0], jrightEyeY[0] );
                }
                else if(index==9){

                    rook_points[1][index] = cv::Point(jrightEyeX[0], jrightEyeY[0]+3 );
                }
                else if(index==10){

                    rook_points[1][index] = cv::Point(jrightEyeX[15], jrightEyeY[15]+1 );
                }
                else if(index==11){

                    rook_points[1][index] = cv::Point(jrightEyeX[14], jrightEyeY[14]+1 );
                }
                else if(index==12){

                    rook_points[1][index] = cv::Point(jrightEyeX[13], jrightEyeY[13]+1 );
                }
                else if(index==13){

                    rook_points[1][index] = cv::Point(jrightEyeX[12], jrightEyeY[12] +1);
                }
                else if(index==14){

                    rook_points[1][index] = cv::Point(jrightEyeX[11], jrightEyeY[11] +1);
                }
                else if(index==15){

                    rook_points[1][index] = cv::Point(jrightEyeX[10], jrightEyeY[10] +2);
                }
                else if(index==16){

                    rook_points[1][index] = cv::Point(jrightEyeX[9], jrightEyeY[9]+2 );
                }

                else if(index==17){

                    rook_points[1][index] = cv::Point(jrightEyeX[8], jrightEyeY[8]+2 );
                }
                else if(index==18){
                    rook_points[1][index] = cv::Point(2*jrightEyeX[8]-jrightEyeX[9], jrightEyeY[7] );

                }
                else if(index==19){
                    rook_points[1][index] = cv::Point(2*jrightEyeX[8]-jrightEyeX[9], jrightEyeY[7]+2 );

                }
//                else if(index==20){
//                    rook_points[1][index] = cv::Point(2*jleftEyeX[14]-jleftEyeX[0], jleftEyeY[3] );
//
//                }
//                else if(index==21){
//                    rook_points[1][index] = cv::Point(2*jleftEyeX[14]-jleftEyeX[0], jleftEyeY[3]+2 );
//
//                }




            }
            const cv::Point* ppt[1] = { rook_points[0] };
            const cv::Point* ppt1[1] = { rook_points[1] };
            int npt[] = { 20 };
            //cv::Mat float_mask(matInput.size(),CV_32FC3,cv::Scalar(0,0,0));
            //cv::Mat mask(matInput.rows, matInput.cols, CV_8UC1, cv::Scalar(0));
            //cv::Mat mask = cv::Mat::zeros(matInput.rows, matInput.cols, CV_32FC3);
            //mask = 0;
//            cv::fillPoly( mask,
//                          ppt,
//                          npt,
//                          1,
//                          cv::Scalar( alpha, alpha, alpha ),
//                          16 );



            //cv::Mat mask  = cv :: Mat::zeros(matInput.size(), CV_8UC1);
            //cv::Mat mask= matInput.clone();
            //mask = 0;
            cv::Mat mask(matInput.size(),CV_32FC3,cv::Scalar(0,0,0));
//            if(leftkajalEye.size() > 2 ) {
//                cv::drawContours(matInput, leftkajalEye, -1, cv::Scalar(255, 255, 255), -1);
//            }

            cv::fillPoly( mask,
                          ppt,
                          npt,
                          1,
                          cv::Scalar( 0.6, 0.6, 0.6 ),
                          8 );
            cv::fillPoly( mask,
                          ppt1,
                          npt,
                          1,
                          cv::Scalar( 0.6, 0.6, 0.6 ),
                          8 );


//            cv::Mat masked(matInput.size(),CV_8UC3,cv:: Scalar(255,255,255));
//            matInput.copyTo(masked,mask);
//            cv::Mat final = cv:: Mat::zeros(matInput.size(), CV_8UC3);
//
//
//            cv :: fillPoly(mask, leftkajalEye, cv :: Scalar(255, 255, 255), 8, 0);
//            cv :: bitwise_and(matInput, matInput, final, mask);

            //cv::Mat color_pallete(mask.size(),CV_32FC3,cv::Scalar( 0, 0, 0 ));
            cv::Mat color_pallete(mask.size(),CV_32FC3,selectedColor);

            matInput.convertTo(matInput, CV_32FC3);
            cv::GaussianBlur(mask,mask,cv::Size(3,3),0);


            cv::multiply(color_pallete,mask,color_pallete);
            cv::subtract(1,mask,mask);
            //cv ::addWeighted(mask, 1, matInput,0.3,0, matInput);
            cv::multiply(matInput,mask,mask);

            cv::add(mask,color_pallete,matInput);
            matInput.convertTo(matInput,CV_8UC3);

            //cv::GaussianBlur(mask,mask,cv::Size(7,7),0,0,3);
            //color_pallete.convertTo(mask, CV_32FC3);
            //cv::multiply(color_pallete,mask,color_pallete,-1);
            //cv::subtract(1,mask,mask);
            //cv::multiply(matInput,mask,mask);
            //cv::add(mask,color_pallete,matInput);
            //matInput.convertTo(matInput,CV_8UC3);

            //LOGD("SEARCH FOR THIS TAG", "%s", mask.size);
            //mask.convertTo(matInput, CV_32FC3);
            //cv ::addWeighted(mask, 0.9, matInput,0.3,0, matInput);




            //apply_eyeLiner(matInput, selectedColor, alpha,leftkajalEye, rightkajalEye);

            //cv::fillConvexPoly(matInput, leftkajalEye, int npts, const Scalar& color, int lineType=8, int shift=0)¶
            //cv ::polylines(matInput, leftkajalEye,true, cv:: Scalar (0,0,0), 1, 8, 0 );
            //int npt[] = { 20 };
            //cv ::fillPoly( matInput,rook_points,npt,true,1,cv ::Scalar( 0, 0, 0 ),8 );



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
//        case MakeupType::earing :{
//            int index = 0;
//            int shift = 2;
//
//            std::vector<cv::Point> leftEar(1);
//            std::vector<cv::Point> rightEar(1);
//
//            int left_ear_nose_x_distance =  (int) (jnoseBridgeX[1]-jfaceX[9] );
//            int right_ear_nose_x_distance =(int) (jfaceX[27]-jnoseBridgeX[1] );
//            float ratio_distance=(float)left_ear_nose_x_distance/(float)right_ear_nose_x_distance;
//            int right_ear_y =  (int) (jfaceY[26]+ jfaceY[27] )/2;
//            int left_ear_Y =(int) (jfaceY[10]+ jfaceY[9])/2;
//            int right_ear_X=0;
//            int left_ear_X=0;
//            leftEar[0] = cv::Point(jfaceX[10], left_ear_Y );
//            rightEar[0] = cv::Point(jfaceX[26],right_ear_y);
//            if(ratio_distance>0.7f && ratio_distance<1.3f){
//                right_ear_y =  (int) (jfaceY[26]+ jfaceY[27] )/2;
//                left_ear_Y =(int) (jfaceY[10]+ jfaceY[9])/2;
//                leftEar[0] = cv::Point(jfaceX[10], left_ear_Y );
//                rightEar[0] = cv::Point(jfaceX[26],right_ear_y);
//                cv :: circle(matInput, leftEar[0],1, cv::Scalar(255,255,255),4, 8,0);
//                cv :: circle(matInput, rightEar[0],1, cv::Scalar(255,255,255),4, 8,0);
//            }
//            else if(ratio_distance<0.7f ){
//                right_ear_y =  (int) (jfaceY[26]+ jfaceY[27] )/2;
//                right_ear_X =(int) (jfaceX[26]-( jfaceY[27]-jfaceY[26] )/2);
//
//                rightEar[0] = cv::Point(right_ear_X,right_ear_y);
//                cv :: circle(matInput, rightEar[0],1, cv::Scalar(255,255,255),4, 8,0);
//
//            }
//            else if(ratio_distance>1.3f){
//                left_ear_Y =(int) (jfaceY[10]+ jfaceY[9])/2;
//                left_ear_X =(int) (jfaceX[10]+( jfaceY[9]-jfaceY[10] )/2);
//
//                leftEar[0] = cv::Point(left_ear_X,left_ear_Y);
//                cv :: circle(matInput, leftEar[0],1, cv::Scalar(255,255,255),4, 8,0);
//
//            }
//
//
//        }


//        case MakeupType  ::earing:{
//
//            int index = 0;
//            int shift = 2;
//            int corner_shift = 0;
//            std::vector<cv::Point> leftEar(1);
//            std::vector<cv::Point> rightEar(1);
//
//
//            leftEar[0] = cv::Point(jfaceX[10], jfaceY[10] );
//            rightEar[0] = cv::Point(jfaceX[26],jfaceY[26]);
//            cv :: circle(matInput, leftEar[0],50, cv::Scalar(255,255,255),4, 8,0);
////            int left_ear_x =  (int) (jfaceX[10] );
////            int left_ear_Y =(int) (jfaceY[10] );
////            int right_ear_x =  (int) (jfaceX[26] );
////            int right_ear_Y =(int) (jfaceY[26] );
//        }
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