
%module lslAndroid
%{
//#include "/Users/yutewang/program/LSL10.13.2016/liblsl/include/lsl_cpp.h";
#include "../../../liblsl/include/lsl_cpp.h"
using namespace lsl;
%}

%include "std_vector.i"
%include "std_string.i"
%include "exception.i"
//%include "/Users/yutewang/program/LSL10.13.2016/liblsl/include/lsl_cpp.h";
%include "../../../liblsl/include/lsl_cpp.h"
namespace std {
   %template(vectorf) vector<float>;
   %template(vectord) vector<double>;
   %template(vectorl) vector<long>;
   %template(vectori) vector<int>;
   %template(vectors) vector<short>;
   %template(vectorstr) vector<string>;
   %template(vectorinfo) vector<lsl::stream_info>;
};


