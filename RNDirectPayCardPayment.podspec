
Pod::Spec.new do |s|
  s.name         = "RNDirectPayCardPayment"
  s.version      = "1.0.0"
  s.summary      = "RNDirectPayCardPayment"
  s.description  = "RNDirectPayCardPayment"
  s.homepage     = "https://directpay.lk"
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/AselaD/react-native-directpay.git", :tag => "master" }
  s.source_files  = "**/*.{h,m,swift}"
  s.requires_arc = true


  s.dependency "React"
  #s.dependency "others"

end

  