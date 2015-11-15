package com.stabilise.entity.component.state;

import com.stabilise.entity.component.Component;
import com.stabilise.util.shape.AABB;


public interface CState extends Component {
    
    AABB getAABB();
    
}
