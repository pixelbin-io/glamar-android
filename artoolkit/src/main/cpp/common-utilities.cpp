//
// Created by imkal001 on 16/9/19.
// Refer Header file for explanation
//
#include <jni.h>
//#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>
//#include <opencv2/imgcodecs.hpp>
#include "tinysplinecpp.h"

bool x_based_ascending_sorting(const cv::Point &i, const cv::Point &j) {
    return i.x < j.x ;
}

bool y_based_ascending_sorting(const cv::Point& i,const cv::Point& j){
    return i.y < j.y ;
}

void fillpoly_x(cv::Mat& image,const std::vector<cv::Point>& points,cv::Scalar color){
    cv::Point p[1][points.size()];
    for (int i = 0; i < points.size(); i++) {
        p[0][i] = points[i];
    }
    const cv::Point *ppt[1] = {p[0]};
    int npt[] = {(int)points.size()};
    cv::fillPoly(image, ppt, npt, 1, color);
}

std::vector<cv::Point> rescale_curve(const std::vector<cv::Point>& points,const float scaling_factor){
    std::vector<cv::Point> expanded_points;
    std::vector<cv::Point> orig_points{points};
    cv::Point2f tmpPoint;
    int min_x,max_x;
    int min_y,max_y;

    if(points.size() < 2){
        return expanded_points;
    }

    //calculate minimum and maximum y-coordinate
    std::sort(orig_points.begin(),orig_points.end(),y_based_ascending_sorting);
    min_y = orig_points[0].y;
    max_y = orig_points[orig_points.size()-1].y;

    //calculating minimum and maximum x-coordinate
    std::sort(orig_points.begin(),orig_points.end(),x_based_ascending_sorting);
    min_x = orig_points[0].x ;
    max_x = orig_points[orig_points.size() - 1].x ;


    cv::Point2f xy_avg = cv::Point2f( (max_x + min_x)/2,(max_y + min_y)/2);

    for (const cv::Point2f & point : orig_points){
        tmpPoint = ( (point - xy_avg) * scaling_factor ) + xy_avg;
        expanded_points.push_back(tmpPoint);
    }
    return expanded_points;
}

void gammaCorrection(cv::Mat &img, const double gamma_)
{
    CV_Assert(gamma_ >= 0);
    //! [changing-contrast-brightness-gamma-correction]
    cv::Mat lookUpTable(1, 256, CV_8U);
    uchar* p = lookUpTable.ptr();
    for( int i = 0; i < 256; ++i)
        p[i] = cv::saturate_cast<uchar>(pow(i / 255.0, gamma_) * 255.0);

    //cv::Mat res = img.clone();
    cv::LUT(img, lookUpTable, img);
}


std::vector<cv::Point> tinyspline_cubic_interpolation(const std::vector<cv::Point>& point_vec){

    std::vector<cv::Point> interpolated_vector ;

    std::vector<cv::Point> points{point_vec};
    std::sort(points.begin(),points.end(),x_based_ascending_sorting);

    int min_x = points[0].x;
    int max_x = points[points.size() - 1 ].x;

    std::vector<tinyspline::real> ts_points;
    ts_points.reserve(2 * points.size());

    for(const auto &point: points){
        ts_points.push_back(point.x);
        ts_points.push_back(point.y);
    }

    tinyspline::BSpline spline = tinyspline::Utils::interpolateCubic(&ts_points, 2);
    for(int x = min_x ; x <= max_x ; x++){
        tinyspline::DeBoorNet net = spline.bisect(x);
        std::vector<tinyspline::real> result = net.result();
        interpolated_vector.push_back( cv::Point(static_cast<int> (result[0]), static_cast<int> (result[1]) ) );
    }

    return interpolated_vector;
}


