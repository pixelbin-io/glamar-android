//
// Created by imkal001 on 16/9/19.
//
#include <jni.h>
#include <algorithm>
//#include <opencv2/imgcodecs.hpp>
#include <opencv2/imgproc.hpp>
#include <vector>
#include <math.h>
#include <android/log.h>

#include "apply-makeup.h"
#include "common-utilities.h"
//#include "tinysplinecpp.h"

#define  LOG_TAG    "someTag"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

void smooth_and_color(cv::Mat& image,cv::Mat& float_mask,cv::Mat& color_pallete,int kernel_size){

    image.convertTo(image, CV_32FC3);
    cv::GaussianBlur(float_mask,float_mask,cv::Size(kernel_size,kernel_size),0);
    cv::multiply(color_pallete,float_mask,color_pallete);
    cv::subtract(1,float_mask,float_mask);
    cv::multiply(image,float_mask,float_mask);
    cv::add(float_mask,color_pallete,image);
    image.convertTo(image,CV_8UC3);

}

void apply_lipstick(cv::Mat &image,const cv::Scalar& color_lips, float alpha,
        const std::vector<cv::Point>& ult_c,const std::vector<cv::Point>& ulb_c,
        const std::vector<cv::Point>& llt_c,const std::vector<cv::Point>& llb_c ){

    int kernel_size =11;
    cv::Mat color_pallete(image.size(),CV_32FC3,color_lips);
    cv::Mat float_mask(image.size(),CV_32FC3,cv::Scalar(0,0,0));


    // for upper lips
    std::vector<cv::Point> points{ult_c};
    points.insert(points.end(), ulb_c.rbegin(), ulb_c.rend());
    // For lower lips
    std::vector<cv::Point> points_2{llb_c};
    points_2.insert(points_2.end(), llt_c.rbegin(), llt_c.rend());

    fillpoly_x(float_mask,points,cv::Scalar(alpha,alpha,alpha));
    fillpoly_x(float_mask,points_2,cv::Scalar(alpha,alpha,alpha));

    smooth_and_color(image,float_mask,color_pallete,kernel_size);
   return;
}

void apply_blush(cv::Mat& image,cv::Scalar color , float alpha,
            cv::Point center_1,cv::Size size_1,double rotation_angle_1, double start_angle_1,double end_angle_1,bool apply_blush_1,
            cv::Point center_2,cv::Size size_2,double rotation_angle_2,double start_angle_2,double end_angle_2,bool apply_blush_2,
            std::vector<cv::Point> face_points) {

    if(image.empty()){

        return;
    }
    int kernel_size = 51;
    cv::Mat color_pallete(image.size(),CV_32FC3,color);
    cv::Mat float_mask(image.size(),CV_32FC3,cv::Scalar(0,0,0));

    if(apply_blush_1) {
        std::vector<cv::Point> ellipse1;
        cv::ellipse2Poly(center_1, size_1, rotation_angle_1, start_angle_1, end_angle_1, 10,
                         ellipse1);
        std::vector<cv::Point> selectedPoints;
        for (auto const &point : ellipse1) {
            if (point.x < face_points[30].x ||
                point.x < face_points[29].x || point.x < face_points[28].x ||
                point.x < face_points[27].x || point.x < face_points[26].x) {
                selectedPoints.push_back(point);
            }
        }
        fillpoly_x(float_mask, selectedPoints, cv::Scalar(alpha, alpha, alpha));
    }

    if(apply_blush_2) {
        std::vector<cv::Point> ellipse2;
        cv::ellipse2Poly(center_2, size_2, rotation_angle_2, start_angle_2, end_angle_2, 10,
                         ellipse2);
        std::vector<cv::Point> selectedPoints_2;
        for (auto const &point : ellipse2) {
            if (point.x > face_points[6].x ||
                point.x > face_points[7].x || point.x > face_points[8].x ||
                point.x > face_points[9].x || point.x > face_points[10].x) {
                selectedPoints_2.push_back(point);
            }
        }
        fillpoly_x(float_mask, selectedPoints_2, cv::Scalar(alpha, alpha, alpha));
    }


    smooth_and_color(image,float_mask,color_pallete,kernel_size);
    return;
}


void apply_eyeLiner(cv::Mat& image,const cv::Scalar& color,float alpha,
                    const std::vector<cv::Point>& left_eye_points,
                    const std::vector<cv::Point>&  right_eye_points )   {

    int kernel_size = 3;
    cv::Mat color_pallete(image.size(),CV_32FC3,color);
    cv::Mat float_mask(image.size(),CV_32FC3,cv::Scalar(0,0,0));

    std::vector<cv::Point> left_eyelid, shifted_lel, right_eyelid, shifted_rel;
    for (int i = 0; i < 9; i++) {
        left_eyelid.push_back(cv::Point(left_eye_points[i].x, left_eye_points[i].y - 2));
        shifted_lel.push_back(cv::Point(left_eye_points[i].x, left_eye_points[i].y - 4));

        right_eyelid.push_back(cv::Point(right_eye_points[i].x, right_eye_points[i].y - 2));
        shifted_rel.push_back(cv::Point(right_eye_points[i].x, right_eye_points[i].y - 4));

    }

    left_eyelid.insert(left_eyelid.end(), shifted_lel.rbegin(), shifted_lel.rend());
    right_eyelid.insert(right_eyelid.end(), shifted_rel.rbegin(), shifted_rel.rend());

    fillpoly_x(float_mask,left_eyelid,cv::Scalar(alpha,alpha,alpha));
    fillpoly_x(float_mask,right_eyelid,cv::Scalar(alpha,alpha,alpha));

    smooth_and_color(image,float_mask,color_pallete,kernel_size);

    return;
}



void apply_eye_shadow_mask(cv::Mat& image,const cv::Scalar & color,float alpha,
                           const std::vector<cv::Point>& l_eyelid, const std::vector<cv::Point>& r_eyelid,
                           const std::vector<cv::Point>&  right_b_eyebrow, const std::vector<cv::Point> & left_b_eyebrow,
                           float m,float n){

    int kernel_size = 31;

    cv::Mat color_pallete(image.size(),CV_32FC3,color);
    cv::Mat float_mask(image.size(),CV_32FC3,cv::Scalar(0,0,0));

    std::vector<cv::Point> right_eyelid{r_eyelid};
    std::vector<cv::Point> left_eyelid{l_eyelid};

    /*
     * The line between corresponding points for upper eyelid and eyebrow is divided in a ratio m:n
     * these new points are taken as upper line for eyeshadow area.
     */
    cv::Point first,second,third,fourth,fifth;
    first = right_eyelid[8] * n + right_b_eyebrow[0] * m;
    second = right_eyelid[6] * n + right_b_eyebrow[1] * m;
    third = right_eyelid[4] * n + right_b_eyebrow[2] * m;
    fourth = right_eyelid[2] *n + right_b_eyebrow[3] * m;
    fifth = right_eyelid[0] *n + right_b_eyebrow[4] * m;
    std::sort(right_eyelid.begin(),right_eyelid.end(),x_based_ascending_sorting);
    right_eyelid.push_back(fifth);
    right_eyelid.push_back(fourth);
    right_eyelid.push_back(third);
    right_eyelid.push_back(second);
    right_eyelid.push_back(first);
    fillpoly_x(float_mask,right_eyelid,cv::Scalar(alpha,alpha,alpha));

    first = left_eyelid[0] * n + left_b_eyebrow[0] * m;
    second = left_eyelid[2] * n + left_b_eyebrow[1] * m;
    third = left_eyelid[4] * n + left_b_eyebrow[2] * m;
    fourth = left_eyelid[6] * n + left_b_eyebrow[3] * m;
    fifth = left_eyelid[8] * n + left_b_eyebrow[4] * m;
    std::sort(left_eyelid.begin(),left_eyelid.end(),x_based_ascending_sorting);
    left_eyelid.push_back(first);
    left_eyelid.push_back(second);
    left_eyelid.push_back(third);
    left_eyelid.push_back(fourth);
    left_eyelid.push_back(fifth);
    fillpoly_x(float_mask,left_eyelid,cv::Scalar(alpha,alpha,alpha));

    smooth_and_color(image,float_mask,color_pallete,kernel_size);

    return;
}



void apply_eyeshadow(cv::Mat& image,const cv::Scalar& color,float alpha,
        const std::vector<cv::Point>& left_eye_points,const std::vector<cv::Point>&  right_eye_points,
        const std::vector<cv::Point>&  right_b_eyebrow,const std::vector<cv::Point>&  left_b_eyebrow ) {


    std::vector<cv::Point> left_eyelid(9) , right_eyelid(9);

    int index = 8;

    /*index = 8 */ right_eyelid[index] = cv::Point(right_eye_points[index].x-5,right_eye_points[index].y); index--;
    /*index = 7 */ right_eyelid[index] = cv::Point(right_eye_points[index].x-1,right_eye_points[index].y-2);index--;
    /*index = 6 */ right_eyelid[index] = cv::Point(right_eye_points[index].x-1,right_eye_points[index].y-2);index--;
    /*index = 5 */ right_eyelid[index] = cv::Point(right_eye_points[index].x-1,right_eye_points[index].y-2);index--;
    /*index = 4 */ right_eyelid[index] = cv::Point(right_eye_points[index].x,right_eye_points[index].y-2);index--;
    /*index = 3 */ right_eyelid[index] = cv::Point(right_eye_points[index].x,right_eye_points[index].y-2);index--;
    /*index = 2 */ right_eyelid[index] = cv::Point(right_eye_points[index].x,right_eye_points[index].y-2);index--;
    /*index = 1 */ right_eyelid[index] = cv::Point(right_eye_points[index].x,right_eye_points[index].y-1);index--;
    /*index = 0 */ right_eyelid[index] = cv::Point(right_eye_points[index].x,right_eye_points[index].y);

    index = 0;
    /*index = 0 */ left_eyelid[index] = cv::Point(left_eye_points[index].x+5,left_eye_points[index].y); index++;
    /*index = 1 */ left_eyelid[index] = cv::Point(left_eye_points[index].x+1,left_eye_points[index].y-2);index++;
    /*index = 2 */ left_eyelid[index] = cv::Point(left_eye_points[index].x+1,left_eye_points[index].y-2);index++;
    /*index = 3 */ left_eyelid[index] = cv::Point(left_eye_points[index].x+1,left_eye_points[index].y-2);index++;
    /*index = 4 */ left_eyelid[index] = cv::Point(left_eye_points[index].x,left_eye_points[index].y-2);index++;
    /*index = 5 */ left_eyelid[index] = cv::Point(left_eye_points[index].x,left_eye_points[index].y-2);index++;
    /*index = 6 */ left_eyelid[index] = cv::Point(left_eye_points[index].x,left_eye_points[index].y-2);index++;
    /*index = 7 */ left_eyelid[index] = cv::Point(left_eye_points[index].x,left_eye_points[index].y-1);index++;
    /*index = 8 */ left_eyelid[index] = cv::Point(left_eye_points[index].x,left_eye_points[index].y);

    float m = 0.8 ;
    apply_eye_shadow_mask(image , color , alpha,
                         left_eyelid , right_eyelid,
                         right_b_eyebrow , left_b_eyebrow,
                         m , 1-m);

    return;
}
