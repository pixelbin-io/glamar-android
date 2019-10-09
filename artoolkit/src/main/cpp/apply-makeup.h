//
// Created by imkal001 on 16/9/19.
//

#ifndef ARBEAUTY2_APPLY_MAKEUP_H
#define ARBEAUTY2_APPLY_MAKEUP_H


void apply_lipstick(cv::Mat &image,const cv::Scalar& color,float alpha,
                    const std::vector<cv::Point>& ult_c,const std::vector<cv::Point>& ulb_c,
                    const std::vector<cv::Point>& llt_c,const std::vector<cv::Point>& llb_c);



void apply_blush(cv::Mat& image,cv::Scalar color , float alpha,
                 cv::Point center_1,cv::Size size_1,double rotation_angle_1, double start_angle_1,double end_angle_1,bool apply_blush_1,
                 cv::Point center_2,cv::Size size_2,double rotation_angle_2,double start_angle_2,double end_angle_2,bool apply_blush_2,
                 std::vector<cv::Point> face_points);

void apply_eye_shadow_mask(cv::Mat& image,const cv::Scalar & color,float alpha,
                           const std::vector<cv::Point>& l_eyelid, const std::vector<cv::Point>& r_eyelid,
                           const std::vector<cv::Point>&  right_b_eyebrow, const std::vector<cv::Point> & left_b_eyebrow,
                           float m,float n);

void apply_eyeshadow(cv::Mat& image,const cv::Scalar& color,float alpha,
                     const std::vector<cv::Point>& left_eye_points,const std::vector<cv::Point>&  right_eye_points,
                     const std::vector<cv::Point>&  right_b_eyebrow,const std::vector<cv::Point>&  left_b_eyebrow );

void apply_eyeLiner(cv::Mat& image,const cv::Scalar& color,float alpha,
                  const std::vector<cv::Point>& left_eye_points,const std::vector<cv::Point>&  right_eye_points );


#endif //ARBEAUTY2_APPLY_MAKEUP_H
