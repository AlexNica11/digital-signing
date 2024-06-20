import { useState } from "react";
import axios from "axios";
import {useNavigate} from "react-router-dom";

const Login = () => {
    const navigate = useNavigate();

    const [input, setInput] = useState({
        email: "",
        username: "",
        password: "",
    });

    const handleSubmitEvent = async (e) => {
        e.preventDefault();
        console.log("email: ", input.email);
        console.log("username: ", input.username);
        console.log("password: ", input.password);
        if (input.username !== "" && input.password !== "") {
            await axios({
                method: 'post',
                url: `/api/users/signup`,
                data: {
                    email: input.email,
                    username: input.username,
                    password: input.password
                },
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json'
                }
            }).then(
                (response) => {
                    navigate("/login");
                }
            ).catch(
                (err) => console.error(err)
            );
        } else {
            alert("please provide a valid input");
        }
    };

    const handleInput = (e) => {
        const { name, value } = e.target;
        setInput((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    return (
    <div className="card">
        <form className="center card-content" onSubmit={handleSubmitEvent}>
            <div className="s12 m6 input-field">
                <input id="email"
                       type="email"
                       className="validate"
                       name="email"
                       aria-describedby="email"
                       aria-invalid="false"
                       placeholder="email@email.com"
                       maxLength="20"
                       onChange={handleInput}/>
                <label htmlFor="email">Email</label>
            </div>
            <div className="s12 m6 input-field">
                <input id="username"
                       type="text"
                       className="validate"
                       name="username"
                       aria-describedby="username"
                       aria-invalid="false"
                       placeholder=" "
                       onChange={handleInput}/>
                <label htmlFor="username">Username</label>
                <span className="supporting-text" data-error="wrong" data-success="right">Your username should be more than 6 character</span>
            </div>
            <div className="s12 m6 input-field">
                <input id="password"
                       type="password"
                       className="validate"
                       name="password"
                       aria-describedby="password"
                       aria-invalid="false"
                       placeholder=" "
                       maxLength="20"
                       onChange={handleInput}/>
                <label htmlFor="password">Password</label>
            </div>
            <button className="btn tonal btn-submit left">Submit</button>
        </form>
    </div>
)
    ;
};

export default Login;
